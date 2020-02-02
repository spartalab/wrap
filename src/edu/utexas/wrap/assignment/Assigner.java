package edu.utexas.wrap.assignment;

import java.util.Collection;

import edu.utexas.wrap.demand.ODMatrix;

public class Assigner<C extends AssignmentContainer> implements Runnable {
	private AssignmentInitializer<C> initializer;
	private AssignmentEvaluator<C> calculator;
	private AssignmentOptimizer<C> optimizer;
	private Collection<C> containers;
	private double threshold;
	
	public void run() {
		while (calculator.getValue(containers.parallelStream()) > threshold) {
			optimizer.optimize(containers.parallelStream());
		}
	}
	

	public void add(ODMatrix<C> matrix) {
		containers = initializer.initialize(matrix.getContainers().parallelStream());
	}
}
