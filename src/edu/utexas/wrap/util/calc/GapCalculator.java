package edu.utexas.wrap.util.calc;

import java.util.Set;

import edu.utexas.wrap.assignment.bush.BushOrigin;
import edu.utexas.wrap.net.Graph;

public class GapCalculator extends Thread {
	public Double val;
	Graph graph;
	Set<BushOrigin> origins;
	LowestCostPathCostCalculator lc;
	TSGCCalculator cc;
	
	public GapCalculator(Graph g, Set<BushOrigin> o, TSGCCalculator tc, LowestCostPathCostCalculator lc) {
		graph = g;
		origins = o;
	}
	
	@Override
	public void run() {
		val = null;

//		Double denominator = 0.0;
		if (cc == null) {
			cc = new TSGCCalculator(graph, origins);
			cc.start();
		}
		if (lc == null) {
			lc = new LowestCostPathCostCalculator(graph,origins);
			lc.start();
		}

		
//		for (BushOrigin o : origins) {
//			for (Bush b : o.getContainers()) {
//				b.shortTopoSearch();
//				Double[] cache = new Double[graph.numNodes()];
//				DemandMap dem = b.getDemandMap(); 
//				for (Node d : b.getNodes()) {
//					
//					Float demand = dem.getOrDefault(d,0.0F);
//					if (demand > 0.0F) try {
//						denominator += b.getCachedL(d,cache) * demand;
//					} catch (UnreachableException e) {
//							e.printStackTrace();
//					}
//				}
//			}
//		}
		try{
			cc.join();lc.join();
			val = (cc.val/lc.val) - 1.0;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}