package edu.utexas.wrap.assignment;

import java.util.Collection;

import edu.utexas.wrap.TimePeriod;
import edu.utexas.wrap.demand.ODProfile;

public class BasicStaticAssigner<C extends AssignmentContainer> implements Assigner {
	private AssignmentEvaluator<C> evaluator;
	private AssignmentOptimizer<C> optimizer;
	private AssignmentInitializer<C> initializer;
	private Collection<C> containers;
	private double threshold;
	private final int maxIterations;
	
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
		// TODO Auto-generated method stub
		TimePeriod timePeriod = TimePeriod.AM_PK;
		
		initializer.add(profile.getMatrix(timePeriod));
	}
	
}
