package edu.utexas.wrap.assignment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.function.ToDoubleFunction;
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
	private Collection<ODMatrix> disaggregatedMtxs;
	private Collection<C> containers;
	private double threshold;
	private final int maxIterations;
	private final Graph network;
	private final TimePeriod tp;
	
	public BasicStaticAssigner(
			AssignmentInitializer<C> initializer,
			AssignmentEvaluator<C> evaluator,
			AssignmentOptimizer<C> optimizer,
			double threshold,
			TimePeriod tp){
		this.initializer = initializer;
		this.evaluator = evaluator;
		this.optimizer = optimizer;
		this.threshold = threshold;
		maxIterations = 100;
		network = null;
		this.tp=tp;
		disaggregatedMtxs = new HashSet<ODMatrix>();
	}
	

	
	public BasicStaticAssigner(Path path, Map<Integer,TravelSurveyZone> zones) throws IOException {
		// TODO Auto-generated constructor stub
	
		Properties props = new Properties();
		props.load(Files.newInputStream(path));
		
		this.network = readNetwork(props, path.getParent(), zones);
		
		Files.createDirectories(Paths.get(network.toString()));
		
		AssignmentProvider<Bush> provider;
		AssignmentConsumer<Bush> primaryConsumer, evaluationConsumer;

		switch (props.getProperty("providerConsumer")) {
		case "bushIOsuite":
			provider = new BushReader(network);
			primaryConsumer = new BushWriter(network);
			evaluationConsumer = new BushForgetter();
			break;
		default:
			throw new RuntimeException("Not yet implented");
		}
		
		
		
		AssignmentBuilder<Bush> builder;
		switch (props.getProperty("builder")) {
		case "bush":
			builder = new BushBuilder(network);
			break;
		default:
			throw new RuntimeException("Not yet implemented");
		}
		
		
		
		switch (props.getProperty("initializer")) {
		case "bush":
			initializer = (AssignmentInitializer<C>) new BushInitializer(provider, primaryConsumer,builder,network);
			break;
		default:
			throw new RuntimeException("Not yet implemented");
		}
		
		
		
		switch (props.getProperty("evaluator")) {
		case "gap":
			evaluator = (AssignmentEvaluator<C>) new GapEvaluator<Bush>(network, provider, evaluationConsumer);
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
			
			
			optimizer = (AssignmentOptimizer<C>) new AlgorithmBOptimizer(
					provider, 
					primaryConsumer, 
					iterEvaluator,
					iterThreshold);
			break;
		default:
			throw new RuntimeException("Not yet implemented");
		}
		
		
		tp = TimePeriod.valueOf(props.getProperty("timePeriod"));
		threshold = Double.parseDouble(props.getProperty("evaluator.threshold"));
		maxIterations = Integer.parseInt(props.getProperty("maxIterations"));
		disaggregatedMtxs = new HashSet<ODMatrix>();
//		throw new RuntimeException("Not finished implementing");
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

	private Collection<ODMatrix> aggregateMtxs(){
		return disaggregatedMtxs.stream()
				.collect(Collectors.groupingBy(this::getMode,
						Collectors.groupingBy(ODMatrix::getVOT,
								Collectors.toSet()
								)
						)
						)
				.entrySet().stream()
				.flatMap(modeEntry -> modeEntry.getValue().entrySet().stream()
						.map(votEntry -> new AddingODMatrix(
								votEntry.getValue(),
								modeEntry.getKey(),
								votEntry.getKey(),
								tp,
								network.getTSZs()
								)
								)
						)
				.collect(Collectors.toSet());
	}

	public void process(ODProfile profile) {
		disaggregatedMtxs.add(profile.getMatrix(getTimePeriod()));
	}
	
	public TimePeriod getTimePeriod() {
		return tp;
	}
	
	public NetworkSkim getSkim(ToDoubleFunction<Link> function) {
		return SkimFactory.calculateSkim(network, function);
	}
	

	private Graph readNetwork(Properties props, Path projDir, Map<Integer,TravelSurveyZone> zoneIDs) {
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

}
