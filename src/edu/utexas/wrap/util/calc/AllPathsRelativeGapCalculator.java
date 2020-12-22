package edu.utexas.wrap.util.calc;

import java.util.Set;

import edu.utexas.wrap.assignment.bush.OldBushOrigin;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;

public class AllPathsRelativeGapCalculator extends Thread {
	public Double val;
	Graph graph;
	Set<TravelSurveyZone> origins;
	LowestCostPathCostCalculator lc;
	TotalSystemGeneralizedCostCalculator cc;
	
	public AllPathsRelativeGapCalculator(Graph g, Set<TravelSurveyZone> o, TotalSystemGeneralizedCostCalculator tc, LowestCostPathCostCalculator lc) {
		graph = g;
		origins = o;
	}
	
	@Override
	public void run() {
		val = null;

		if (cc == null) {
			cc = new TotalSystemGeneralizedCostCalculator(graph, origins);
			cc.start();
		}
		if (lc == null) {
			lc = new LowestCostPathCostCalculator(graph,origins);
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