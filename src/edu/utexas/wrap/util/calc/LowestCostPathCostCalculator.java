package edu.utexas.wrap.util.calc;

import java.util.Set;

import edu.utexas.wrap.assignment.AssignmentContainer;
import edu.utexas.wrap.net.Graph;

public class LowestCostPathCostCalculator extends Thread {
	public Graph graph;
	public Set<AssignmentContainer> containers;
	public Double val;

	public LowestCostPathCostCalculator(Graph g, Set<AssignmentContainer> containers) {
		graph = g;
		this.containers = containers;
	}
	public void run() {
		val = null;
		val = containers.parallelStream().mapToDouble(b -> graph.cheapestCostPossible(b)).sum();
	}
}