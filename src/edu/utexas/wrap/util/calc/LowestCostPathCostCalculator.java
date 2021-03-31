package edu.utexas.wrap.util.calc;

import java.util.Set;

import edu.utexas.wrap.assignment.bush.Bush;
import edu.utexas.wrap.net.Graph;

public class LowestCostPathCostCalculator extends Thread {
	public Graph graph;
	public Set<Bush> bushes;
	public Double val;

	public LowestCostPathCostCalculator(Graph g, Set<Bush> origins) {
		graph = g;
		this.bushes = origins;
	}
	public void run() {
		val = null;
		val = bushes.parallelStream().mapToDouble(b -> b.lowestCostPathCost()).sum();
	}
}