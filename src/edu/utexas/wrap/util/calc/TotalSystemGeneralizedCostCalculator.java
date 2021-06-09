package edu.utexas.wrap.util.calc;

import java.util.Set;

import edu.utexas.wrap.assignment.AssignmentContainer;
import edu.utexas.wrap.assignment.bush.Bush;
import edu.utexas.wrap.net.Graph;

public class TotalSystemGeneralizedCostCalculator extends Thread {
	public Double val;
	Graph graph;
	Set<AssignmentContainer> origins;
	
	public TotalSystemGeneralizedCostCalculator(Graph g, Set<AssignmentContainer> o) {
		graph = g;
		origins = o;
	}
	
	@Override
	public void run() {
		val = origins.parallelStream()
				.filter(c -> c instanceof Bush).map(c -> (Bush) c)
				.mapToDouble(b -> b.incurredCost()).sum();
	}
}