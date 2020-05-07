package edu.utexas.wrap.assignment.bush;

import edu.utexas.wrap.net.Graph;

public class BushGapEvaluator implements BushEvaluator {
	
	private final Graph graph;
	
	public BushGapEvaluator(Graph g) {
		graph = g;
	}

	@Override
	public double getValue(Bush bush) {
		//TODO the denominator should actually only depend on used paths rather than all paths
		Double num = bush.incurredCost();
		Double denom = graph.cheapestCostPossible(bush);
		if (num.isNaN() || denom.isNaN() || ((Double) (num/denom)).isNaN()) 
			throw new RuntimeException();
		return num/denom - 1.0;
	}

}
