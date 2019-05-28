package edu.utexas.wrap.util.calc;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.utexas.wrap.assignment.bush.Bush;
import edu.utexas.wrap.assignment.bush.BushOrigin;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;
import edu.utexas.wrap.util.UnreachableException;

public class GapCalculator extends Thread {
	public Double val;
	Graph graph; 
	Set<BushOrigin> origins;
	TSGCCalculator cc;
	
	public GapCalculator(Graph g, Set<BushOrigin> o, TSGCCalculator tc) {
		graph = g;
		origins = o;
	}
	
	@Override
	public void run() {
		val = null;

		Double denominator = 0.0;
		if (cc == null) {
			cc = new TSGCCalculator(graph, origins);
		cc.start();
		}

		
		for (BushOrigin o : origins) {
			for (Bush b : o.getContainers()) {
				b.shortTopoSearch();
				Map<Node, Double> cache = new HashMap<Node, Double>(graph.numNodes());
				Map<Node, Double> dem = b.getDemandMap().doubleClone(); 
				for (Node d : b.getNodes()) {
					
					Float demand = dem.getOrDefault(d,0.0).floatValue();
					if (demand > 0.0F) try {
						denominator += b.getCachedL(d,cache).doubleValue() * demand;
					} catch (UnreachableException e) {
							e.printStackTrace();
					}
				}
			}
		}
		try{
			cc.join();
			val = (cc.val/denominator) - 1.0;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}