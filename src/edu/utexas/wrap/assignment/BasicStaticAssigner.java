/*
    wrap - free and open-source urban transportation modeling software
    Copyright (C) 2017 the wrap project, The University of Texas at Austin

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.utexas.wrap.assignment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import edu.utexas.wrap.TimePeriod;
import edu.utexas.wrap.assignment.bush.Bush;
import edu.utexas.wrap.assignment.bush.BushBuilder;
import edu.utexas.wrap.assignment.bush.BushEvaluator;
import edu.utexas.wrap.assignment.bush.BushForgetter;
import edu.utexas.wrap.assignment.bush.BushGapEvaluator;
import edu.utexas.wrap.assignment.bush.BushInitializer;
import edu.utexas.wrap.assignment.bush.BushReader;
import edu.utexas.wrap.assignment.bush.BushWriter;
import edu.utexas.wrap.assignment.bush.algoB.AlgorithmBOptimizer;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.demand.ODProfile;
import edu.utexas.wrap.demand.containers.AddingODMatrix;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.NetworkSkim;
import edu.utexas.wrap.net.TravelSurveyZone;
import edu.utexas.wrap.util.io.GraphFactory;
import edu.utexas.wrap.util.io.SkimFactory;

public class BasicStaticAssigner<C extends AssignmentContainer> implements StaticAssigner {
	private AssignmentEvaluator<C> evaluator;
	private AssignmentOptimizer<C> optimizer;
	private AssignmentInitializer<C> initializer;
	private Map<ODMatrix,Float> disaggregatedMtxs;
	private Collection<C> containers;
	private double threshold;
	private final int maxIterations;
	private final Graph network;
	private final TimePeriod tp;
	private final String name;
	
	private BasicStaticAssigner(String name,
			AssignmentEvaluator<C> evaluator,
			AssignmentOptimizer<C> optimizer,
			AssignmentInitializer<C> initializer,
			double threshold,
			int maxIterations,
			Graph network,
			TimePeriod tp
			){
		this.name = name;
		this.evaluator = evaluator;
		this.optimizer = optimizer;
		this.initializer = initializer;
		this.threshold = threshold;
		this.maxIterations = maxIterations;
		this.network = network;
		this.tp = tp;
		disaggregatedMtxs = new HashMap<ODMatrix,Float>();

	}


	public static BasicStaticAssigner<?> fromPropsFile(String name, Path path, Map<Integer,TravelSurveyZone> zones) throws IOException {
		// TODO Auto-generated constructor stub

		Properties props = new Properties();
		props.load(Files.newInputStream(path));

		Graph network = readNetwork(props, path.getParent(), zones);
		TimePeriod tp = TimePeriod.valueOf(props.getProperty("timePeriod"));
		Double threshold = Double.parseDouble(props.getProperty("evaluator.threshold"));
		Integer maxIterations = Integer.parseInt(props.getProperty("maxIterations"));

		Files.createDirectories(Paths.get(network.toString()));


		switch (props.getProperty("containerType")) {

		case "bush":
			return bushAssignerFromProps(name, props, network, tp, threshold, maxIterations);

		case "path":
			
		case "link":
			
		default:
			throw new RuntimeException("Unknown containerType: " + props.getProperty("containerType"));
		}

	}


	private static BasicStaticAssigner<Bush> bushAssignerFromProps(String name, Properties props, Graph network, TimePeriod tp,
			Double threshold, Integer maxIterations) throws IOException {
		AssignmentProvider<Bush> provider;
		AssignmentConsumer<Bush> writer, forgetter;
		AssignmentEvaluator<Bush> evaluator;
		AssignmentOptimizer<Bush> optimizer;
		AssignmentInitializer<Bush> initializer;
		AssignmentBuilder<Bush> builder;
		
		
		switch (props.getProperty("providerConsumer")) {
		case "bushIOsuite":
			//TODO custom paths
			Path ioPath = Paths.get(props.getProperty("providerConsumer.source"));
			provider = new BushReader(network,ioPath);
			writer = new BushWriter(network,ioPath);
			forgetter = new BushForgetter();
			break;
		default:
			throw new RuntimeException("Not yet implented");
		}



		switch (props.getProperty("builder")) {
		case "bush":
			builder = new BushBuilder(network);
			break;
		default:
			throw new RuntimeException("Not yet implemented");
		}



		switch (props.getProperty("initializer")) {
		case "bush":
			initializer = new BushInitializer(provider, writer, forgetter, builder,network);
			break;
		default:
			throw new RuntimeException("Not yet implemented");
		}



		switch (props.getProperty("evaluator")) {
		case "gap":
			evaluator = new GapEvaluator<Bush>(network, provider, forgetter);
			break;
		default:
			throw new RuntimeException("Not yet implemented");
		}



		switch (props.getProperty("optimizer")) {
		case "algoB":
			BushEvaluator iterEvaluator;
			
			switch(props.getProperty("optimizer.iterEvaluator")) {
			case "bushGap":
				iterEvaluator = new BushGapEvaluator(network);
				break;
			default:
				throw new RuntimeException("Not yet implemented");
			}

			double iterThreshold = Double.parseDouble(props.getProperty("optimizer.iterThreshold"));

			optimizer = new AlgorithmBOptimizer(
					provider, 
					writer, 
					iterEvaluator,
					iterThreshold);
			break;
		default:
			throw new RuntimeException("Not yet implemented");
		}



		return new BasicStaticAssigner<Bush>(name, evaluator, optimizer, initializer, threshold, maxIterations, network, tp);
	}


	public void run() {
		System.out.println("Aggregating OD matrices for "+tp);
		aggregateMtxs().forEach(initializer::add);

		System.out.println("Initializing bushes for "+tp);
		this.containers = initializer.initializeContainers();

		int numIterations = 0;
		double value = evaluator.getValue(containers.parallelStream()); 
		while (value > threshold && numIterations < maxIterations) {
			System.out.println("Iteration "+numIterations++ + "\tValue: "+value);
			optimizer.optimize(containers.stream());
			value = evaluator.getValue(containers.parallelStream());
		}
		System.out.println("Final value: "+value);

	}

	private Map<ODMatrix,Float> aggregateMtxs(){

		Function<Mode,Collector<ODMatrix,?,ODMatrix>> e = mode -> Collectors.collectingAndThen(
				Collectors.toSet(), 
				set -> new AddingODMatrix(set,mode, tp, network.getTSZs()
						)
				);
		
		return disaggregatedMtxs.entrySet().stream()
		//aggregate matrices into a mapping based on aggregate mode and value of time, then store in a set
		.collect(						
				Collectors.groupingBy(Map.Entry::getValue,
						Collectors.mapping(Map.Entry::getKey,
								Collectors.groupingBy(this::getMode,
										Collectors.toSet()
										)
								)
						)
				)
		//for each value of time,
		.entrySet().parallelStream()
		.flatMap(
				//for each mode
				votEntry -> votEntry.getValue().entrySet().stream()
				.map(
						//collect all into an AddingODMatrix (which is Mode-aware)
						modeEntry -> modeEntry.getValue().stream()
						.collect(
								e.apply(modeEntry.getKey()))
						)
				//map each AddingODMatrix to its VOT
				.map(
						odm -> new AbstractMap.SimpleEntry<ODMatrix, Float>(odm, votEntry.getKey())
						)
				)
		//then collect into a map
		.collect(
				Collectors.toMap(
						Map.Entry::getKey, 
						Map.Entry::getValue
						)
				);
	}

	public void process(ODProfile profile) {
		disaggregatedMtxs.put(
				profile.getMatrix(getTimePeriod()),
				profile.getVOT(getTimePeriod()));
	}

	public TimePeriod getTimePeriod() {
		return tp;
	}

	public NetworkSkim getSkim(ToDoubleFunction<Link> function) {
		return SkimFactory.calculateSkim(network, function);
	}


	private static Graph readNetwork(Properties props, Path projDir, Map<Integer,TravelSurveyZone> zoneIDs) {
		System.out.println("Reading network");

		try {
			File linkFile = projDir.resolve(props.getProperty("network.links")).toFile();

			//			Map<Integer, AreaClass> zoneClasses = getAreaClasses();

			switch(props.getProperty("network.linkType")) {

			case "conic":

				//				Integer ftn = Integer.parseInt(props.getProperty("network.firstThruNode"));
				return GraphFactory.readConicGraph(linkFile, zoneIDs);

			case "bpr":
				Graph g = GraphFactory.readTNTPGraph(linkFile, zoneIDs);


				//FIXME This assumes the lowest n indices are the Node id's, but this isn't guaranteed
				//				AtomicInteger idx = new AtomicInteger(0);
				//				zones.entrySet().parallelStream()
				//				.forEach(entry -> {
				//					Node n = g.getNode(entry.getKey());
				//					TravelSurveyZone z = new TravelSurveyZone(n,idx.getAndIncrement(),entry.getValue());
				//					n.setTravelSurveyZone(z);
				//					g.addZone(z);
				//				});
				//				g.setNumZones(idx.get());

				return g;
			default:
				throw new IllegalArgumentException("network.type");
			}

		} catch (NullPointerException e) {
			System.err.println("Missing property: network.linkFile");
			System.exit(-2);
			return null;

		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			System.err.println("Invalid property value: network.firstThruNode\r\nCould not parse integer");
			System.exit(-3);
			return null;

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.err.println("Invalid property value: network.linkFile\r\nFile not found");
			e.printStackTrace();
			System.exit(-4);
			return null;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("Error while reading network files");
			System.exit(-5);
			return null;

		} catch (IllegalArgumentException e) {
			System.err.println("Illegal argument: "+e.getMessage());
			System.exit(-6);
			return null;
		}

	}

	private Mode getMode(ODMatrix mtx) {
		switch (mtx.getMode()) {
		case HOV_2_PSGR:
		case HOV_3_PSGR:
		case HOV:
			return Mode.HOV;

		default:
			return mtx.getMode();
		}
	}

	public void outputFlows(Path outputFile) {
		// TODO Auto-generated method stub
		try {
			BufferedWriter writer = Files.newBufferedWriter(
					outputFile,
					StandardOpenOption.CREATE, 
					StandardOpenOption.TRUNCATE_EXISTING);

			network.getLinks().parallelStream()
			.map(link -> link.toString()+","+link.getFlow()+","+link.getTravelTime()+"\r\n")
			.forEach(line -> {
				try {
					writer.write(line);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String toString() {
		return name;
	}
}
