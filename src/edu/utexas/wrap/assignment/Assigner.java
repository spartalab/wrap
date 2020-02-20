package edu.utexas.wrap.assignment;

import java.util.Collection;

public class Assigner<C extends AssignmentContainer> implements Runnable {
	private AssignmentEvaluator<C> evaluator;
	private AssignmentOptimizer<C> optimizer;
	private Collection<C> containers;
	private double threshold;
	
	public Assigner(
			AssignmentInitializer<C> initializer,
			AssignmentEvaluator<C> evaluator,
			AssignmentOptimizer<C> optimizer){
		
		this.containers = initializer.initializeContainers();
		this.evaluator = evaluator;
		this.optimizer = optimizer;
		
	}
	
	public void run() {
		
		while (evaluator.getValue(containers.parallelStream()) > threshold) {
			optimizer.optimize(containers.parallelStream());
		}
		
	}
	
}
