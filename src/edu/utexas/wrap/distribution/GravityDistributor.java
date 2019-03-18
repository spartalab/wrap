package edu.utexas.wrap.distribution;

import java.util.HashMap;
import java.util.Map;

import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.demand.containers.AggregateODHashMatrix;
import edu.utexas.wrap.demand.containers.AggregatePAHashMatrix;
import edu.utexas.wrap.demand.containers.DemandHashMap;
import edu.utexas.wrap.net.Node;

public class GravityDistributor extends TripDistributor {
	private FrictionFactorMap friction;
	
	public GravityDistributor(FrictionFactorMap fm) {
		friction = fm;
	}
	@Override
	public AggregatePAMatrix distribute(PAMap pa) {
		Map<Integer, Double> a = new HashMap<Integer,Double>();
		Map<Integer, Double> b = new HashMap<Integer, Double>();
		AggregatePAMatrix pam = new AggregatePAHashMatrix();
		Boolean converged = false;
		while (!converged) {
			converged = true;
			
			for (Integer i : pa.getProducers()) {
				Double denom = 0.0;
				
				for (Integer z : pa.getAttractors()) {
					denom += b.getOrDefault(z, 1.0) * pa.getAttractions(z) * friction.get(i,z);
				}
				
				if (!a.containsKey(i) || !converged(a.get(i), 1.0/denom)) {
					converged = false;
					a.put(i, 1.0/denom);
				}
			}
		
			for (Integer j : pa.getAttractors()) {
				Double denom = 0.0;
				for (Integer z : pa.getProducers()) {
					denom += a.get(z) * pa.getProductions(z) * friction.get(j, z);
				}
				
				if (!b.containsKey(j) || !converged(b.get(j), 1.0/denom)) {
					converged = false;
					b.put(j, 1.0/denom);
				}
			}
		}
		
		for (Integer i : pa.getProducers()) {
			DemandHashMap d = new DemandHashMap();
			
			for (Integer j : pa.getAttractors()) {
				d.put(j, (float) (a.get(i)*pa.getProductions(i)*b.get(j)*pa.getAttractions(j)*friction.get(i, j)));
			}
			
			pam.putDemand(i, d);
		}
		return pam;
	}
	
	private Boolean converged(Double a, Double b) {
		Double margin = 2*Math.max(Math.ulp(a), Math.ulp(b));
		return  ((a < b && b-a < margin) || a-b < margin); 
	}
	//TODO Use JDBC to write out the PA Map into the actual database


}
