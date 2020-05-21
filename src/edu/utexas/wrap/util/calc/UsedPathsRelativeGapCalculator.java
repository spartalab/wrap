package edu.utexas.wrap.util.calc;

import java.util.Set;
import java.util.concurrent.atomic.DoubleAdder;

import edu.utexas.wrap.assignment.bush.OldBushOrigin;
import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;
import edu.utexas.wrap.util.UnreachableException;

public class UsedPathsRelativeGapCalculator extends Thread {
	public Double val;
	Graph graph;
	Set<OldBushOrigin> origins;
	TotalSystemGeneralizedCostCalculator cc;
	
	public UsedPathsRelativeGapCalculator(Graph g, Set<OldBushOrigin> o, TotalSystemGeneralizedCostCalculator tc) {
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
		DoubleAdder denominator = new DoubleAdder();
		
		origins.parallelStream().flatMap(o -> o.getContainers().parallelStream()).forEach(b ->{
			b.shortTopoSearch();
			Double[] cache = new Double[graph.numNodes()];
			DemandMap dem = b.getDemandMap(); 
			b.getNodes().parallelStream().forEach(d -> {
				TravelSurveyZone tsz = d.getZone();
				if (tsz == null) return;
				Float demand = dem.get(d.getZone());
				if (demand > 0.0F) try {
					denominator.add(b.getCachedL(d,cache) * demand);
				} catch (UnreachableException e) {
						e.printStackTrace();
				}
			});
		});

		try{
			cc.join();
			val = (cc.val/denominator.sum()) - 1.0;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
