package edu.utexas.wrap.util.calc;

import java.util.Map;
import java.util.Set;

import edu.utexas.wrap.assignment.AssignmentContainer;
import edu.utexas.wrap.assignment.Origin;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Link;

public class TSGCCalculator extends Thread {
	public Double val;
	Graph graph;
	Set<? extends Origin> origins;
	
	public TSGCCalculator(Graph g, Set<? extends Origin> o) {
		graph = g;
		origins = o;
	}
	
	@Override
	public void run() {
		double tsgc = 0.0;
		
		for (Origin o : origins) {
			for (AssignmentContainer b : o.getContainers()) {
				Map<Link,Double> flows = b.getFlows();
				for (Link l : b.getUsedLinks()) {
					tsgc += flows.getOrDefault(l, 0.0) * l.getPrice(b.getVOT(),b.getVehicleClass());
				}
			}
		}
		val =  tsgc;
	}
}