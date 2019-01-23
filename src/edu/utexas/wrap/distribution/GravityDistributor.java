package edu.utexas.wrap.distribution;

import java.util.HashMap;
import java.util.Map;

import edu.utexas.wrap.demand.AggregateOriginDestinationMatrix;
import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.ProductionAttractionMap;
import edu.utexas.wrap.net.Node;

public class GravityDistributor extends TripDistributor {
	private FrictionFactorMap friction;
	
	public GravityDistributor(FrictionFactorMap fm) {
		friction = fm;
	}
	@Override
	public AggregateOriginDestinationMatrix distribute(ProductionAttractionMap pa) {
		Map<Node, Double> a = new HashMap<Node,Double>();
		Map<Node, Double> b = new HashMap<Node,Double>();
		AggregateOriginDestinationMatrix od = new AggregateOriginDestinationMatrix();
		Boolean converged = false;
		while (!converged) {
			converged = true;
			
			for (Node i : pa.getProducers()) {
				Double denom = 0.0;
				
				for (Node z : pa.getAttractors()) {
					denom += b.getOrDefault(z, 1.0) * pa.getAttractions(z) * friction.get(i,z);
				}
				
				if (!a.containsKey(i) || !converged(a.get(i), 1.0/denom)) {
					converged = false;
					a.put(i, 1.0/denom);
				}
			}
		
			for (Node j : pa.getAttractors()) {
				Double denom = 0.0;
				for (Node z : pa.getProducers()) {
					denom += a.get(z) * pa.getProductions(z) * friction.get(j, z);
				}
				
				if (!b.containsKey(j) || !converged(b.get(j), 1.0/denom)) {
					converged = false;
					b.put(j, 1.0/denom);
				}
			}
		}
		
		for (Node i : pa.getProducers()) {
			DemandMap d = new DemandMap();
			
			for (Node j : pa.getAttractors()) {
				d.put(j, (float) (a.get(i)*pa.getProductions(i)*b.get(j)*pa.getAttractions(j)*friction.get(i, j)));
			}
			
			od.put(i, d);
		}
		return od;
	}
	
	private Boolean converged(Double a, Double b) {
		Double margin = 2*Math.max(Math.ulp(a), Math.ulp(b));
		return  ((a < b && b-a < margin) || a-b < margin); 
	}

}
