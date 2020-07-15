package edu.utexas.wrap.assignment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Properties;
import java.util.function.ToDoubleFunction;

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
import edu.utexas.wrap.demand.ODProfile;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.NetworkSkim;
import edu.utexas.wrap.util.io.SkimFactory;

public class BasicStaticAssigner<C extends AssignmentContainer> implements StaticAssigner {
	private AssignmentEvaluator<C> evaluator;
	private AssignmentOptimizer<C> optimizer;
	private AssignmentInitializer<C> initializer;
	private Collection<C> containers;
	private double threshold;
	private final int maxIterations;
	private final Graph network;
	
	public BasicStaticAssigner(
			AssignmentInitializer<C> initializer,
			AssignmentEvaluator<C> evaluator,
			AssignmentOptimizer<C> optimizer,
			double threshold){
		this.initializer = initializer;
		this.evaluator = evaluator;
		this.optimizer = optimizer;
		this.threshold = threshold;
		maxIterations = 100;
		network = null;
	}
	

	
	public BasicStaticAssigner(Graph network, Path path) throws IOException {
		// TODO Auto-generated constructor stub
		this.network = network;
		Properties props = new Properties();
		props.load(Files.newInputStream(path));
		
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
		
		
		
		threshold = Double.parseDouble(props.getProperty("evaluator.threshold"));
		maxIterations = Integer.parseInt(props.getProperty("maxIterations"));
//		throw new RuntimeException("Not finished implementing");
	}



	public void run() {
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



	@Override
	public void process(ODProfile profile) {
		initializer.add(profile.getMatrix(getTimePeriod()));
	}
	
	public TimePeriod getTimePeriod() {
		return TimePeriod.AM_PK;
	}
	
	public NetworkSkim getSkim(ToDoubleFunction<Link> function) {
		return SkimFactory.calculateSkim(network, function);
	}

}
