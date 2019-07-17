package edu.utexas.wrap.util.calc;

import edu.utexas.wrap.net.Graph;

public class BeckmannCalculator extends Thread {
	public Double val;
	Graph graph;
	
	public BeckmannCalculator(Graph g) {
		graph = g;
	}
	
	@Override
	public void run() {
		val = graph.getLinks().parallelStream().mapToDouble(x -> x.tIntegral()).sum();

	}
}