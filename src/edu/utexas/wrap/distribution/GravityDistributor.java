package edu.utexas.wrap.distribution;

import java.util.Map;

import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.demand.containers.AggregatePAHashMatrix;
import edu.utexas.wrap.demand.containers.DemandHashMap;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;
import it.unimi.dsi.fastutil.objects.Object2DoubleMaps;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;

public class GravityDistributor extends TripDistributor {
	private FrictionFactorMap friction;
	private Graph g;
	Boolean converged = false;

	public GravityDistributor(Graph g, FrictionFactorMap fm) {
		this.g = g;
		friction = fm;
	}
	@Override
	public AggregatePAMatrix distribute(PAMap pa) {
		Map<Node, Double> a = Object2DoubleMaps.synchronize(new Object2DoubleOpenHashMap<Node>(g.numZones(),1.0f));
		Map<Node, Double> b = Object2DoubleMaps.synchronize(new Object2DoubleOpenHashMap<Node>(g.numZones(),1.0f));
		AggregatePAHashMatrix pam = new AggregatePAHashMatrix(g);

		int iterations = 0;
		while (!converged && iterations < 100) {
			converged = true;
			iterations++;
			System.out.println("Iteration "+iterations);
			
			pa.getProducers().parallelStream().forEach(i -> {
				Double denom = pa.getAttractors().parallelStream().mapToDouble(x -> b.getOrDefault(x, 1.0)*pa.getAttractions(x)*friction.get(i,x)).sum();
				if (denom == 0.0 || denom.isNaN()) throw new RuntimeException();
				if (!a.containsKey(i) || !converged(a.getOrDefault(i,1.0), 1.0/denom)) {
					converged = false;
					a.put(i, 1.0/denom);
				}
			});

			pa.getAttractors().forEach(j->{
				Double denom = pa.getProducers().parallelStream().mapToDouble(x-> a.getOrDefault(x, 1.0)*pa.getProductions(x)*friction.get(x,j)).sum();
				if (denom == 0.0 || denom.isNaN()) throw new RuntimeException();
				if (!b.containsKey(j) || !converged(b.getOrDefault(j,1.0), 1.0/denom)) {
					converged = false;
					b.put(j, 1.0/denom);
				}	
			});

		}
		
		pa.getProducers().parallelStream().forEach(i -> {
			DemandHashMap d = new DemandHashMap(g);

			
			for (Node j : pa.getAttractors()) {
				Double Tij =  (a.get(i)*pa.getProductions(i)*b.get(j)*pa.getAttractions(j)*friction.get(i, j));
				if (Tij.isNaN()) throw new RuntimeException();
				d.put(j, Tij.floatValue());
			}

			pam.putDemandMap(i, d);
		});

		return pam;
	}

	private Boolean converged(Double a, Double b) {
		Double margin = 0.001;
		return Math.abs(a-b) < margin;
	}
}
