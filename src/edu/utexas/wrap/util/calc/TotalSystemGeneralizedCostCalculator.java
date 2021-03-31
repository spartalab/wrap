package edu.utexas.wrap.util.calc;

import java.util.Set;

import edu.utexas.wrap.assignment.bush.Bush;
import edu.utexas.wrap.net.Graph;

public class TotalSystemGeneralizedCostCalculator extends Thread {
	public Double val;
	Graph graph;
	Set<Bush> bushes;
	
	public TotalSystemGeneralizedCostCalculator(Graph g, Set<Bush> o) {
		graph = g;
		bushes = o;
	}
	
	@Override
	public void run() {
		val = bushes.parallelStream()
				.mapToDouble(b -> b.incurredCost()).sum();
	}
}