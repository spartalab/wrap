package edu.utexas.wrap.util.calc;

import java.util.Set;

import edu.utexas.wrap.assignment.bush.BushOrigin;
import edu.utexas.wrap.net.Graph;

public class LowestCostPathCostCalculator extends Thread {
	public Graph graph;
	public Set<BushOrigin> origins;
	public Double val;

	public LowestCostPathCostCalculator(Graph g, Set<BushOrigin> origins) {
		graph = g;
		this.origins = origins;
	}
	public void run() {
		val = null;
		val = origins.parallelStream().flatMap(o -> o.getContainers().parallelStream()).mapToDouble(b -> b.lowestCostPathCost()).sum();
	}
}