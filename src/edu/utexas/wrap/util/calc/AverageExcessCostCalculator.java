package edu.utexas.wrap.util.calc;

import java.util.Set;

import edu.utexas.wrap.assignment.bush.OldBushOrigin;
import edu.utexas.wrap.net.Graph;

public class AverageExcessCostCalculator extends Thread {
	public Double val;
	Graph graph;
	Set<OldBushOrigin> origins;
	TotalSystemGeneralizedCostCalculator cc;
	LowestCostPathCostCalculator lc;
	
	public AverageExcessCostCalculator(Graph g, Set<OldBushOrigin> o, TotalSystemGeneralizedCostCalculator tc, LowestCostPathCostCalculator lc) {
		graph = g;
		origins = o;
		this.cc = tc;
		this.lc = lc;
	}
	
	@Override
	public void run() {
		//TODO: Modify for generalized cost
		if (cc == null) {
			cc = new TotalSystemGeneralizedCostCalculator(graph, origins);
			cc.start();
		}
		if (lc == null) {
			lc = new LowestCostPathCostCalculator(graph,origins);
			lc.start();
		}
		
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