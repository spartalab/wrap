package edu.utexas.wrap.util.calc;

import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Link;

public class BeckmannCalculator extends Thread {
	public Double val;
	Graph graph;
	
	public BeckmannCalculator(Graph g) {
		graph = g;
	}
	
	@Override
	public void run() {
		val = null;
		Double b = 0.0;
		for (Link l : graph.getLinks()) {
			b += l.tIntegral().doubleValue();
		}
		val = b;
	}
}