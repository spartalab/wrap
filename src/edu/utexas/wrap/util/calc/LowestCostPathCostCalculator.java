package edu.utexas.wrap.util.calc;

import java.util.Set;

import edu.utexas.wrap.assignment.AssignmentContainer;
import edu.utexas.wrap.net.Graph;

public class LowestCostPathCostCalculator extends Thread {
	public Graph graph;
	public Set<AssignmentContainer> origins;
	public Double val;

	public LowestCostPathCostCalculator(Graph g, Set<AssignmentContainer> origins) {
		graph = g;
		this.origins = origins;
	}
	public void run() {
		val = null;
		val = origins.parallelStream().mapToDouble(b -> b.lowestCostPathCost()).sum();
	}
}