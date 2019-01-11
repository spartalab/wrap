package edu.utexas.wrap.util.calc;

import java.util.Set;

import edu.utexas.wrap.assignment.Bush;
import edu.utexas.wrap.assignment.Origin;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Link;

public class TSGCCalculator extends Thread {
	public Double val;
	Graph graph;
	Set<Origin> origins;
	
	public TSGCCalculator(Graph g, Set<Origin> o) {
		graph = g;
		origins = o;
	}
	
	@Override
	public void run() {
		double tsgc = 0.0;
		
		for (Origin o : origins) {
			for (Bush b : o.getBushes()) {
				for (Link l : b) {
					tsgc += l.getFlow(b).doubleValue() * l.getPrice(b.getVOT(),b.getVehicleClass()).doubleValue();
				}
			}
		}
		val =  tsgc;
	}
}