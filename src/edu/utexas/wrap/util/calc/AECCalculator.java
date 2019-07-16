package edu.utexas.wrap.util.calc;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.utexas.wrap.assignment.bush.Bush;
import edu.utexas.wrap.assignment.bush.BushOrigin;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;
import edu.utexas.wrap.util.UnreachableException;

public class AECCalculator extends Thread {
	public Double val;
	Graph graph;
	Set<BushOrigin> origins;
	TSGCCalculator cc;
	LowestCostPathCostCalculator lc;
	
	public AECCalculator(Graph g, Set<BushOrigin> o, TSGCCalculator tc, LowestCostPathCostCalculator lc) {
		graph = g;
		origins = o;
		this.cc = tc;
		this.lc = lc;
	}
	
	@Override
	public void run() {
		//TODO: Modify for generalized cost
		if (cc == null) {
			cc = new TSGCCalculator(graph, origins);
			cc.start();
		}
		if (lc == null) {
			lc = new LowestCostPathCostCalculator(graph,origins);
			lc.start();
		}

//		Double numerator = 0.0;
//		Double denominator = 0.0;
//		
//		for (BushOrigin o : origins) {
//			for (Bush b : o.getContainers()) {
//				Map<Node,Double> od = b.getDemandMap().doubleClone();
//				for (Node d : od.keySet()) {
//					Double demand = o.getDemand(d);
//					if (demand > 0.0) {
//						Double[] cache = new Double[graph.numNodes()];
//						try {
//							numerator -= b.getCachedL(d, cache).doubleValue() * demand;
//						} catch (UnreachableException e) {
//							if (demand > 0) e.printStackTrace();
//						}
//						denominator += demand;
//					}
//				}
//			}
//		}
		
		Double demand = origins.parallelStream().flatMap(o -> o.getContainers().parallelStream()).mapToDouble(b -> b.totalDemand()).sum();
		
		val = null;
		try {
			cc.join();
			lc.join();
			val = (cc.val-lc.val)/demand;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
}