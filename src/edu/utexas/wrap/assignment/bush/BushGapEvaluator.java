package edu.utexas.wrap.assignment.bush;

import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.Node;

public class BushGapEvaluator implements BushEvaluator {
	
	private final Graph graph;
	
	public BushGapEvaluator(Graph g) {
		graph = g;
	}

	@Override
	public double getValue(Bush bush) {
		Double num = bush.incurredCost();
		Double denom = cheapestCostPossible(bush);
		if (num.isNaN() || denom.isNaN() || (((Double) (num/denom)).isNaN() && !num.equals(denom))) 
			throw new RuntimeException();
		else if (((Double) (num/denom)).isNaN())
			return 0;
		return num/denom - 1.0;
	}
	
	private Double cheapestCostPossible(Bush bush) {
		Node[] to = bush.getTopologicalOrder(true);
		double[] latent = new double[graph.numNodes()];
		PathCostCalculator pcc = new PathCostCalculator(bush);
		
		Double val = 0.0;
		
		for (int i = to.length-1; i > 0; i--) {
			if (to[i] == null) continue;
			double toDemand = bush.getDemand(to[i]) + latent[to[i].getOrder()];
			Link q = pcc.getqShort(to[i]);
			double linkCost = q.getPrice(bush);
			
			latent[q.getTail().getOrder()] += toDemand;
			
			val += linkCost*toDemand;
		}
		return val;
	}

}
