package edu.utexas.wrap.distribution;


import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.demand.containers.FixedSizeAggregatePAMatrix;
import edu.utexas.wrap.demand.containers.FixedSizeDemandMap;
import edu.utexas.wrap.net.NetworkSkim;
import edu.utexas.wrap.net.TravelSurveyZone;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

/**A tweaked version of the GravityDistributor that allows for
 * on-demand iterations of fitting.
 * 
 * @author Carlin, William
 *
 */
public class ModularGravityDistributor extends GravityDistributor {
	private final NetworkSkim skim;
	private final FrictionFactorMap friction;
	private final Collection<TravelSurveyZone> zones;

	private final Double[] a;
	private final Double[] b;
	private final PAMap pa;
	private final AtomicBoolean converged;
	private final Double margin;

	public ModularGravityDistributor(Collection<TravelSurveyZone> zones, NetworkSkim skim, FrictionFactorMap fm,
									 PAMap pa, Double margin) {
		super(zones, skim, fm);

		this.zones = zones;
		friction = fm;
		this.skim = skim;

		a = new Double[zones.size()];
		b = new Double[zones.size()];
		this.pa = pa;
		converged = new AtomicBoolean(false);
		this.margin = margin;
	}

	private Boolean converged(Double a, Double b) {
		return Math.abs(a-b) < margin;
	}

	public void iterateProductions() {
		//For each producer

		converged.set(true);

		zones.parallelStream().forEach(i -> {
			//Calculate a new denominator as sum_attractors(attractions*impedance*b)
			Double denom = zones.stream()
					.mapToDouble(x ->
							(b[x.getOrder()] == null? 1.0 : b[x.getOrder()])
									*pa.getAttractions(x)*friction.get(skim.getCost(i, x)))
					.sum();

			//Check for errors
			if (denom == 0.0 || denom.isNaN()) throw new RuntimeException();

			//Write a new A value for this producer
			Double temp = a[i.getOrder()];
			a[i.getOrder()] = 1.0/denom;

			//If no A value exists yet or this is not within the numerical tolerance of the previous value
			if (temp == null || !converged(temp, a[i.getOrder()])) {
				converged.set(false);
			}
		});
	}

	public void iterateAttractions() {

		converged.set(true);

		//For each attractor
		zones.parallelStream().forEach(j->{

			//Calculate a new denominator as sum_producers(productions*impedance*a)
			Double denom = zones.stream()
					.mapToDouble(x->
							(a[x.getOrder()] == null? 1.0 : a[x.getOrder()])
									*pa.getProductions(x)*friction.get(skim.getCost(x, j)))
					.sum();

			//Check for errors
			if (denom == 0.0 || denom.isNaN()) throw new RuntimeException();

			//Write a new B value for this attractor
			Double temp = b[j.getOrder()];
			b[j.getOrder()] = 1.0/denom;

			//If no B value exists yet or this is not within the numerical tolerance of the previous value
			if (temp == null || !converged(temp, b[j.getOrder()])) {
				converged.set(false);
			}
		});

	}
	
	public AggregatePAMatrix getMatrix() {
		
		//Now begin constructing the matrix
		FixedSizeAggregatePAMatrix pam = new FixedSizeAggregatePAMatrix(zones);
		//For each producer
		zones.parallelStream().forEach(producer -> {
			//Construct a new DemandMap
			DemandMap d = new FixedSizeDemandMap(zones);
			//For each attractor
			for (TravelSurveyZone attractor : zones) {
				//Calculate the number of trips between these two as a*productions*b*attractions*impedance
				Double Tij = 
						a[producer.getOrder()]
						*pa.getProductions(producer)
						*b[attractor.getOrder()]
						*pa.getAttractions(attractor)
						*friction.get(skim.getCost(producer, attractor));
				
				//Check for errors
				if (Tij.isNaN()) throw new RuntimeException();
				//Store this value as the number of trips in the DemandMap
				d.put(attractor, Tij.floatValue());
			}

			//Store the DemandMap in the new matrix
			pam.putDemandMap(producer, d);
		});

		return pam;
	}

	public Boolean isConverged() {
		return converged.get();
	}
}
