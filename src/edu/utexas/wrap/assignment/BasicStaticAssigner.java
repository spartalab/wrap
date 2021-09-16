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
import java.util.stream.Stream;

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
	private final Collection<Mode> modes;
	private final Path projDir;
	private double threshold;
	private final int maxIterations;
	private final Graph network;
	private final TimePeriod tp;
	private final String name;
	private double lastEvaluation;
	private int iterationsPerformed;
	private final Properties props;
	
	private BasicStaticAssigner(String name,
			Path projDir,
			Properties props,
			Graph network,
			AssignmentEvaluator<C> evaluator,
			AssignmentOptimizer<C> optimizer,
			AssignmentInitializer<C> initializer
			){
		this.name = name;
		this.evaluator = evaluator;
		this.optimizer = optimizer;
		this.initializer = initializer;
		this.projDir = projDir;
		this.props = props;
		
		this.threshold = Double.parseDouble(props.getProperty("evaluator.threshold"));
		this.maxIterations = Integer.parseInt(props.getProperty("maxIterations"));
		this.network = network;
		this.tp = TimePeriod.valueOf(props.getProperty("timePeriod"));
		this.modes = Stream.of(props.getProperty("modes").split(","))
				.map(mode -> Mode.valueOf(mode))
				.collect(Collectors.toSet());


		disaggregatedMtxs = new HashMap<ODMatrix,Float>();

	}


	public static BasicStaticAssigner<?> fromPropsFile(String name, Path projectPath, String propertiesFile, Map<Integer,TravelSurveyZone> zones) throws IOException {
		// TODO Auto-generated constructor stub

		Properties props = new Properties();
		props.load(Files.newInputStream(projectPath.resolve(propertiesFile)));

		switch (props.getProperty("containerType")) {

		case "bush":
			return bushAssignerFromProps(name, projectPath, props, zones);

		case "path":
			
		case "link":
			
		default:
			throw new RuntimeException("Unknown containerType: " + props.getProperty("containerType"));
		}

	}


	private static BasicStaticAssigner<Bush> bushAssignerFromProps(String name, Path projectPath, Properties props,Map<Integer,TravelSurveyZone> zones) throws IOException {
		AssignmentProvider<Bush> provider;
		AssignmentConsumer<Bush> writer, forgetter;
		AssignmentEvaluator<Bush> evaluator;
		AssignmentOptimizer<Bush> optimizer;
		AssignmentInitializer<Bush> initializer;
		AssignmentBuilder<Bush> builder;
		
		Graph network = new Graph(zones);
		
		
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


		return new BasicStaticAssigner<Bush>(name, projectPath, props, network, evaluator, optimizer, initializer);
	}

	public void iterate() {
		optimizer.optimize(containers.stream());
		iterationsPerformed++;
	}


	private double evaluate() {
		return evaluator.getValue(containers.parallelStream());
	}
	
	public boolean isTerminated() {
		return lastEvaluation <= threshold;
	}


	public void initialize() {
		try {			
			File linkFile = projDir.resolve(props.getProperty("network.links")).toFile();

			switch(props.getProperty("network.linkType")) {

			case "conic":

				GraphFactory.readConicLinks(linkFile, network);
				break;

			case "bpr":
				GraphFactory.readTNTPLinks(linkFile, network);
				break;

			default:
				throw new IllegalArgumentException("network.type");
			}
			
			Files.createDirectories(Paths.get(props.getProperty("providerConsumer.source"))
					.resolve(network.toString()));

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		iterationsPerformed = 0;
		aggregateMtxs().forEach(initializer::add);

		this.containers = initializer.initializeContainers();
	}
	
	public double getProgress() {
		lastEvaluation = evaluate();
		double iterationProgress = iterationsPerformed/maxIterations;
		double objectiveProgress = 1/Math.pow(2, Math.log10(lastEvaluation/threshold));
		
		return Math.min(1,Math.max(objectiveProgress,iterationProgress));
	}
	

	private Map<ODMatrix,Float> aggregateMtxs(){

		Function<Mode,Collector<ODMatrix,?,ODMatrix>> e = mode -> Collectors.collectingAndThen(
				Collectors.toSet(), 
				set -> new AddingODMatrix(set,mode, tp, network.getTSZs()
						)
				);
		
		return disaggregatedMtxs.entrySet().stream()
				.filter(entry -> modes.contains(getMode(entry.getKey())))
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
	

	public NetworkSkim getSkim(String id, ToDoubleFunction<Link> function) {
		return SkimFactory.calculateSkim(network, function, id);
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
