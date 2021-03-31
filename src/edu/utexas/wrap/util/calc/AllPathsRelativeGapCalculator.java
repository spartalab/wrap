package edu.utexas.wrap.util.calc;

import java.util.Set;

import edu.utexas.wrap.assignment.bush.Bush;
import edu.utexas.wrap.net.Graph;

public class AllPathsRelativeGapCalculator extends Thread {
	public Double val;
	Graph graph;
	Set<Bush> bushes;
	LowestCostPathCostCalculator lc;
	TotalSystemGeneralizedCostCalculator cc;
	
	public AllPathsRelativeGapCalculator(Graph g, Set<Bush> o, TotalSystemGeneralizedCostCalculator tc, LowestCostPathCostCalculator lc) {
		graph = g;
		bushes = o;
	}
	
	@Override
	public void run() {
		val = null;

		if (cc == null) {
			cc = new TotalSystemGeneralizedCostCalculator(graph, bushes);
			cc.start();
		}
		if (lc == null) {
			lc = new LowestCostPathCostCalculator(graph,bushes);
			lc.start();
		}

		try{
			cc.join();
			lc.join();
			val = (cc.val/lc.val) - 1.0;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}