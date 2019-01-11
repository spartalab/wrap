package edu.utexas.wrap.util.calc;

import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Link;

public class TSTTCalculator extends Thread {
	public Double val;
	Graph graph;
	
	public TSTTCalculator(Graph g) {
		graph = g;
	}
	
	@Override
	public void run() {
		Double tstt = 0.0;
		
		for(Link l: graph.getLinks()){
			tstt += l.getFlow().doubleValue() * l.getTravelTime().doubleValue();
		}
		val = tstt;
	}
}