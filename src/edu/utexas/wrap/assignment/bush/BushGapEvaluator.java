package edu.utexas.wrap.assignment.bush;

import edu.utexas.wrap.net.Graph;

public class BushGapEvaluator implements BushEvaluator {
	
	private final Graph graph;
	
	public BushGapEvaluator(Graph g) {
		graph = g;
	}

	@Override
	public double getValue(Bush bush) {
		//TODO determine whether the denominator should actually only depend on used paths rather than all paths
		return bush.incurredCost()/graph.cheapestCostPossible(bush);
	}

}
