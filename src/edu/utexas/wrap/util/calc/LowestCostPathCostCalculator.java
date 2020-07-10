package edu.utexas.wrap.util.calc;

import java.util.Set;

import edu.utexas.wrap.assignment.bush.OldBushOrigin;
import edu.utexas.wrap.net.Graph;

public class LowestCostPathCostCalculator extends Thread {
	public Graph graph;
	public Set<OldBushOrigin> origins;
	public Double val;

	public LowestCostPathCostCalculator(Graph g, Set<OldBushOrigin> origins) {
		graph = g;
		this.origins = origins;
	}
	public void run() {
		val = null;
		val = origins.parallelStream().flatMap(o -> o.getContainers().parallelStream()).mapToDouble(b -> b.lowestCostPathCost()).sum();
	}
}