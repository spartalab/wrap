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

import java.io.BufferedReader;
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
import edu.utexas.wrap.assignment.bush.AlternateSegmentPair;
import edu.utexas.wrap.assignment.bush.Bush;
import edu.utexas.wrap.assignment.bush.BushBuilder;
import edu.utexas.wrap.assignment.bush.BushEvaluator;
import edu.utexas.wrap.assignment.bush.BushForgetter;
import edu.utexas.wrap.assignment.bush.BushGapEvaluator;
import edu.utexas.wrap.assignment.bush.BushInitializer;
import edu.utexas.wrap.assignment.bush.BushReader;
import edu.utexas.wrap.assignment.bush.BushWriter;
import edu.utexas.wrap.assignment.bush.algoB.AlgorithmBOptimizer;
import edu.utexas.wrap.assignment.bush.signalized.SignalizedOptimizer;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.demand.ODProfile;
import edu.utexas.wrap.demand.containers.AddingODMatrix;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.CentroidConnector;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.NetworkSkim;
import edu.utexas.wrap.net.TolledBPRLink;
import edu.utexas.wrap.net.TolledEnhancedLink;
import edu.utexas.wrap.net.TravelSurveyZone;
import edu.utexas.wrap.util.io.GraphFactory;
import edu.utexas.wrap.util.io.SkimFactory;

public class BasicStaticAssigner<C extends AssignmentContainer> implements StaticAssigner<C> {
	private AssignmentEvaluator<C> evaluator;
	private AssignmentOptimizer<C> optimizer;
	private AssignmentInitializer<C> initializer;
	private Map<ODMatrix,Float> disaggregatedMtxs;
	private Collection<C> containers;
	private final Collection<Mode> modes;
	private double threshold;
	private final int maxIterations;
	private Graph network;
	private final TimePeriod tp;
	private final String name;
	private double lastEvaluation;
	private int iterationsPerformed;
	private ToDoubleFunction<Link> tollingPolicy;
	private final Class<? extends Link> linkType;
	private final Path containerSource, linkSource, 
		cycleLengthSource, cycleSplitSource, signalGroupSource, ringSource,ringShareSource,linkedMvmtSource;
	private final PressureFunction pressureFunction;
	private final Map<Integer, TravelSurveyZone> zones;
	
	private BasicStaticAssigner(String name,
			Map<Integer, TravelSurveyZone> zones,
			AssignmentEvaluator<C> evaluator,
			AssignmentOptimizer<C> optimizer,
			AssignmentInitializer<C> initializer,
			Class<? extends Link> linkType,
			Double threshold,
			Integer maxIterations,
		TimePeriod tp,
		Collection<Mode> modes,
		Path containerSource,
		Path linkSource,
		Path cycleLengthSource,
		Path cycleSplitSource,
		Path signalGroupSource,
		Path ringSource,
		Path ringShareSource,
		Path linkedMvmtSource,
		PressureFunction pressure
			){
		this.name = name;
		this.evaluator = evaluator;
		this.optimizer = optimizer;
		this.initializer = initializer;
		
		this.threshold = threshold;
		this.maxIterations = maxIterations;
		this.tp = tp;
		this.modes = modes;
		this.linkType = linkType;
		this.linkSource= linkSource;
		this.cycleLengthSource = cycleLengthSource;
		this.cycleSplitSource = cycleSplitSource;
		this.containerSource = containerSource;
		this.signalGroupSource = signalGroupSource;
		this.ringSource = ringSource;
		this.ringShareSource = ringShareSource;
		this.linkedMvmtSource = linkedMvmtSource;
		this.zones = zones;
		this.pressureFunction = pressure;
	}


	public static BasicStaticAssigner<?> fromPropsFile(String name, Path projectPath, String propertiesFile, Map<Integer,TravelSurveyZone> zones) throws IOException {
		// TODO Auto-generated constructor stub

		Properties props = new Properties();
		props.load(Files.newInputStream(projectPath.resolve(propertiesFile)));

		switch (props.getProperty("containerType")) {

		case "bush":
			return bushAssignerFromProps(name, zones, projectPath, props);

		case "path":
			
		case "link":
			
		default:
			throw new RuntimeException("Unknown containerType: " + props.getProperty("containerType"));
		}

	}


	private static BasicStaticAssigner<Bush> bushAssignerFromProps(String name,
			Map<Integer, TravelSurveyZone> zones, Path projectPath, Properties props) throws IOException {
		AssignmentProvider<Bush> provider;
		AssignmentConsumer<Bush> writer, forgetter;
		AssignmentEvaluator<Bush> evaluator;
		AssignmentOptimizer<Bush> optimizer;
		AssignmentInitializer<Bush> initializer;
		AssignmentBuilder<Bush> builder;
		PressureFunction pressureFunction = null;
		Class<? extends Link> linkType;
		
	
		switch (props.getProperty("providerConsumer")) {
		case "bushIOsuite":
			//TODO custom paths
			Path ioPath = Paths.get(props.getProperty("providerConsumer.source"));
			provider = new BushReader(ioPath);
			writer = new BushWriter(ioPath);
			forgetter = new BushForgetter();
			break;
		default:
			throw new RuntimeException("Not yet implented");
		}



		switch (props.getProperty("builder")) {
		case "bush":
			builder = new BushBuilder();
			break;
		default:
			throw new RuntimeException("Not yet implemented");
		}



		switch (props.getProperty("initializer")) {
		case "bush":
			initializer = new BushInitializer(provider, writer, forgetter, builder);
			break;
		default:
			throw new RuntimeException("Not yet implemented");
		}



		switch (props.getProperty("evaluator")) {
		case "gap":
			evaluator = new GapEvaluator<Bush>(provider, forgetter);
			break;
		default:
			throw new RuntimeException("Not yet implemented");
		}


		BushEvaluator iterEvaluator;
		
		switch(props.getProperty("optimizer.iterEvaluator")) {
		case "bushGap":
			iterEvaluator = new BushGapEvaluator();
			break;
		default:
			throw new RuntimeException("Not yet implemented");
		}

		double iterThreshold = Double.parseDouble(props.getProperty("optimizer.iterThreshold"));


		switch (props.getProperty("optimizer")) {
		case "algoB":

			optimizer = new AlgorithmBOptimizer(
					provider, 
					writer, 
					iterEvaluator,
					iterThreshold,
					AlternateSegmentPair::getDerivativeSum);
			break;
		case "signalized":
			switch (props.getProperty("optimizer.pressureFunc")) {
			case "P0":
				pressureFunction = new P0();
				break;
			case "WLYM":
				pressureFunction = new WLYM();
				break;
			case "Alexander":
				pressureFunction = new Alexander();
				break;
			default:
				throw new RuntimeException("Not yet implemented");
			}
			
			optimizer = new SignalizedOptimizer(
					provider,
					writer,
					iterEvaluator,
					iterThreshold,
					pressureFunction);
			break;
		default:
			throw new RuntimeException("Not yet implemented");
		}
		
		switch (props.getProperty("network.linkType")) {
		case "conic":
			linkType = TolledEnhancedLink.class;
			break;
		case "bpr":
			linkType = TolledBPRLink.class;
			break;
		default:
			linkType = CentroidConnector.class;
		}

		Double threshold = Double.parseDouble(
				props.getProperty("evaluator.threshold"));
		Integer maxIterations = Integer.parseInt(
				props.getProperty("maxIterations"));
		TimePeriod tp = TimePeriod.valueOf(
				props.getProperty("timePeriod"));
		Collection<Mode> modes = Stream.of(
				props.getProperty("modes").split(","))
				.map(mode -> Mode.valueOf(mode))
				.collect(Collectors.toSet());
		Path linkSource = projectPath.resolve(
				props.getProperty("network.links"));
		Path containerSource = Paths.get(
				props.getProperty("providerConsumer.source"));
		
		// make the below optional
		Path cycleLengthSource = null, 
				cycleSplitSource = null,
				signalGroupSource = null,
				ringSource = null,
				ringShareSource = null,
				linkedMvmtSource = null;
		try {
		 cycleLengthSource = Paths.get(
				props.getProperty("signalTimings.cycleLengths"));
		 cycleSplitSource = Paths.get(
				props.getProperty("signalTimings.cycleSplits")
				);
		 signalGroupSource = Paths.get(
				props.getProperty("signalTimings.signalGroups")
				);
		 ringSource = Paths.get(
				 props.getProperty("signalTimings.rings")
				 );
		 ringShareSource = Paths.get(
				 props.getProperty("signalTimings.ringShares"));
		 linkedMvmtSource = Paths.get(props.getProperty("signalTimings.linkedMovements"));
		 
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new BasicStaticAssigner<Bush>(
				name, zones, 
				evaluator, optimizer, initializer, 
				linkType, 
				threshold, maxIterations, tp, 
				modes, 
				containerSource, linkSource,
				cycleLengthSource,cycleSplitSource,
				signalGroupSource,ringSource, ringShareSource,linkedMvmtSource,
				pressureFunction);
	}


//	public boolean isTerminated() {
//		return lastEvaluation <= threshold || iterationsPerformed >= maxIterations;
//	}
	
	public PressureFunction getPressureFunction() {
		return pressureFunction;
	}

	public void initialize(Collection<ODProfile> profiles) {
		disaggregatedMtxs = new HashMap<ODMatrix,Float>();
		profiles.forEach(this::process);
		if (tollingPolicy == null) tollingPolicy = link -> 0.0;

		network = new Graph(zones);
		
		//TODO get tolling policy
		try {			
			File linkFile = linkSource.toFile();
			
			Map<Integer, Float> greenShares = null;
			Map<Integer, Float> cycleLengths = null;
			Map<Integer, Map<Integer,Integer>> signalGroups = null;
			Map<Integer, Map<Integer,Integer>> rings = null;
			Map<Integer,Map<Integer,Double>> ringShares = null;
			Map<Integer,Map<Integer,Integer[]>> linkedMvmts = null;
			
			if (cycleSplitSource != null 
					&& cycleLengthSource != null
					&& signalGroupSource != null
					&& ringSource != null
					&& ringShareSource != null
					&& linkedMvmtSource != null
					) {
				greenShares = getSplits();
				cycleLengths = getCycleLengths();
				signalGroups = getSignalGroups();
				rings = getRings();
				ringShares = getRingShares();
				linkedMvmts = getLinkedMvmts();
			}
			

			if (TolledEnhancedLink.class.isAssignableFrom(linkType))
				GraphFactory.readConicLinks(linkFile, network, tollingPolicy,pressureFunction);
			else if (TolledBPRLink.class.isAssignableFrom(linkType))
				GraphFactory.readTNTPLinks(
						linkFile, 
						network, 
						tollingPolicy, pressureFunction,
						greenShares, cycleLengths, signalGroups, rings,
						ringShares, linkedMvmts);

			
			Files.createDirectories(containerSource
					.resolve(network.toString()));

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		iterationsPerformed = 0;
		aggregateMtxs().forEach( (mtx, vot) -> initializer.add(network, mtx, vot));
		this.containers = initializer.initializeContainers(network);
	}
	
	private Map<Integer,Map<Integer,Integer[]>> getLinkedMvmts() throws IOException {
		BufferedReader lf = Files.newBufferedReader(linkedMvmtSource);
		lf.readLine();
		return lf.lines().map(line -> line.split(",")).collect(
				Collectors.groupingBy(args -> Integer.parseInt(args[0]), 
						Collectors.toMap(args -> Integer.parseInt(args[1]), 
								args-> {
									Integer[] params = {
											Integer.parseInt(args[2]),
											Integer.parseInt(args[3]),
											Integer.parseInt(args[4])};
									return params;
								}
						)));
//		List<Integer[]> ret = lf.lines().map(line -> line.split(","))
//		.map(args -> new Integer[]{Integer.parseInt(args[0]),
//		              Integer.parseInt(args[1]),
//		              Integer.parseInt(args[2]),
//		              Integer.parseInt(args[3]),
//		              Integer.parseInt(args[4])})
//		.collect(Collectors.toList());
//		return ret.toArray(new Integer[ret.size()][]);
		
	}
	
	private Map<Integer, Map<Integer, Double>> getRingShares() throws IOException {
		// TODO Auto-generated method stub
		BufferedReader lf = Files.newBufferedReader(ringShareSource);
		lf.readLine();
		return lf.lines().parallel()
				.map(line -> line.split(","))
				.collect(Collectors.groupingBy(
						args -> Integer.parseInt(args[0]),
						Collectors.toMap(args -> Integer.parseInt(args[1]), 
								args -> Double.parseDouble(args[2]))));
	}


	private Map<Integer, Map<Integer, Integer>> getRings() throws IOException {
		// TODO Auto-generated method stub
		BufferedReader lf = Files.newBufferedReader(ringSource);
		lf.readLine();
		return lf.lines().parallel()
				.map(line->line.split(","))
				.collect(Collectors.groupingBy(
						args-> Integer.parseInt(args[0]),
						Collectors.toMap(
								args -> Integer.parseInt(args[1]),
								args -> Integer.parseInt(args[2]))));
	}


	private Map<Integer, Map<Integer,Integer>> getSignalGroups()
			throws IOException {
//		// TODO Auto-generated method stub
		BufferedReader lf = Files.newBufferedReader(signalGroupSource);
		lf.readLine();
		return lf.lines().parallel()
				.map(line->line.split(","))
				.collect(Collectors.groupingBy(
						args -> Integer.parseInt(args[0]), 
						Collectors.toMap(
								args -> Integer.parseInt(args[1]), 
								args -> Integer.parseInt(args[2]))));
	}


	private Map<Integer, Float> getCycleLengths() throws IOException {
		// TODO Auto-generated method stub
		BufferedReader lf = Files.newBufferedReader(cycleLengthSource);
		
		return lf.lines().parallel().collect(
				Collectors.toMap(
						line -> Integer.parseInt(line.split(",")[0]),
						line -> Float.parseFloat(line.split(",")[1])));
		
	}


	private Map<Integer, Float> getSplits() throws IOException {
		// TODO Auto-generated method stub
		BufferedReader lf = Files.newBufferedReader(cycleSplitSource);
		
		return lf.lines().parallel().collect(
				Collectors.toMap(
						line -> Integer.parseInt(line.split(",")[0]), 
						line -> Float.parseFloat(line.split(",")[1])));
	}


	public double getProgress(double currentValue, int numIterations) {
		lastEvaluation = currentValue;
		iterationsPerformed = numIterations;
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
				//disregard matrices which use a mode not handled by this assigner
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
	

	private void process(ODProfile profile) {
		disaggregatedMtxs.put(
				profile.getMatrix(getTimePeriod()),
				profile.getVOT(getTimePeriod()));
	}
	

	public TimePeriod getTimePeriod() {
		return tp;
	}
	
	public NetworkSkim getSkim(String id, ToDoubleFunction<Link> function) {
		System.out.println("Getting skim from assigner for "+id);
		return SkimFactory.calculateSkim(network, function, id);
	}

	public Mode getMode(ODMatrix mtx) {
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


	@Override
	public Class<? extends Link> getLinkType(){
		return linkType;
	}


	@Override
	public Collection<Mode> assignedModes() {
		// TODO Auto-generated method stub
		return modes;
	}
	
	@Override
	public Integer maxIterations() {
		return maxIterations;
	}
	
	public void setTollingPolicy(ToDoubleFunction<Link> policy) {
		tollingPolicy = policy;
	}

	@Override
	public AssignmentEvaluator<C> getEvaluator() {
		return evaluator;
	}
	
	@Override
	public AssignmentOptimizer<C> getOptimizer() {
		return optimizer;
	}

	@Override
	public Graph getNetwork() {
		// TODO Auto-generated method stub
		return network;
	}
	
	public Collection<C> getContainers() {
		return containers;
	}

	public Path getContainerSource() {
		return containerSource;
	}

}
