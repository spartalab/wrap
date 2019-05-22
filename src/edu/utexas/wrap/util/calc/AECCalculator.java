package edu.utexas.wrap.util.calc;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.utexas.wrap.assignment.bush.Bush;
import edu.utexas.wrap.assignment.bush.BushOrigin;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;
import edu.utexas.wrap.util.UnreachableException;

class AECCalculator extends Thread {
	Double val;
	Graph graph;
	Set<BushOrigin> origins;
	TSGCCalculator cc;
	
	public AECCalculator(Graph g, Set<BushOrigin> o, TSGCCalculator tc) {
		graph = g;
		origins = o;
		this.cc = tc;
	}
	
	@Override
	public void run() {
		//TODO: Modify for generalized cost
		if (cc == null) {
			cc = new TSGCCalculator(graph, origins);
			cc.start();
		}

		Double numerator = 0.0;
		Double denominator = 0.0;
		
		for (BushOrigin o : origins) {
			for (Bush b : o.getContainers()) {
				for (Node d : b.getNodes()) {
					Double demand = o.getDemand(d);
					if (demand > 0.0) {
						Map<Node, Double> cache = new HashMap<Node, Double>();
						try {
							numerator -= b.getCachedL(d, cache).doubleValue() * demand;
						} catch (UnreachableException e) {
							if (demand > 0) e.printStackTrace();
						}
						denominator += demand;
					}
				}
			}
		}
		val = null;
		try {
			cc.join();
			numerator += cc.val;
			val = numerator/denominator;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
}