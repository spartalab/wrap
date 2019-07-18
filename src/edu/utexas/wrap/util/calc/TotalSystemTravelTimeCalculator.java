package edu.utexas.wrap.util.calc;

import edu.utexas.wrap.net.Graph;

public class TotalSystemTravelTimeCalculator extends Thread {
	public Double val;
	Graph graph;
	
	public TotalSystemTravelTimeCalculator(Graph g) {
		graph = g;
	}
	
	@Override
	public void run() {
		val = graph.getLinks().parallelStream().mapToDouble(x -> x.getFlow()*x.getTravelTime()).sum();
	}
}