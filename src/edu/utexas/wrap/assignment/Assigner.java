package edu.utexas.wrap.assignment;

import java.util.Collection;

public class Assigner<C extends AssignmentContainer> implements Runnable {
	private AssignmentEvaluator<C> evaluator;
	private AssignmentOptimizer<C> optimizer;
	private Collection<C> containers;
	private double threshold;
	private final int maxIterations;
	
	public Assigner(
			AssignmentInitializer<C> initializer,
			AssignmentEvaluator<C> evaluator,
			AssignmentOptimizer<C> optimizer){
		
		this.containers = initializer.initializeContainers();
		this.evaluator = evaluator;
		this.optimizer = optimizer;
		maxIterations = 10;
		
	}
	
	public void run() {
		int numIterations = 0;
		double value = evaluator.getValue(containers.parallelStream()); 
		while (value > threshold && numIterations < maxIterations) {
			System.out.println("Iteration "+numIterations++ + "\tValue: "+value);
			optimizer.optimize(containers.parallelStream());
			value = evaluator.getValue(containers.parallelStream());
		}
		System.out.println("Final value: "+value);
	}
	
}
