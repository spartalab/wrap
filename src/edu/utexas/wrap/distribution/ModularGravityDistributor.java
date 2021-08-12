/*
    wrap - free and open-source urban transportation modeling software
    Copyright (C) 2017 the wrap project, The University of Texas at Austin

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.utexas.wrap.distribution;


import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.demand.containers.FixedSizeAggregatePAMatrix;
import edu.utexas.wrap.demand.containers.FixedSizeDemandMap;
import edu.utexas.wrap.marketsegmentation.Purpose;
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
	private final FrictionFactorMap friction;

	private final PAMap pa;
	private final AtomicBoolean converged;
	private final Double margin;

	public ModularGravityDistributor(String name, Purpose parent, Double scalingFactor, FrictionFactorMap fm, DistributionWeights weights,
									 PAMap pa, Double margin) {
		super(name, parent, scalingFactor, fm, weights);

		friction = fm;

		this.pa = pa;
		converged = new AtomicBoolean(false);
		this.margin = margin;
	}

	private Boolean converged(Double a, Double b) {
		return Math.abs(a-b) < margin;
	}

	public void iterateProductions(NetworkSkim skim) {
		//For each producer

		converged.set(true);
		Collection<TravelSurveyZone> zones = purpose.getMarket().getZones().values();
		zones.parallelStream().forEach(i -> {
			//Calculate a new denominator as sum_attractors(attractions*impedance*b)
			Double denom = zones.stream()
					.mapToDouble(x ->
							(attractorWeights[x.getOrder()] == null? 1.0 : attractorWeights[x.getOrder()])
									*pa.getAttractions(x)*friction.get(skim.getCost(i, x)))
					.sum();

			//Check for errors
			if (denom == 0.0 || denom.isNaN()) throw new RuntimeException();

			//Write a new A value for this producer
			Double temp = producerWeights[i.getOrder()];
			producerWeights[i.getOrder()] = 1.0/denom;

			//If no A value exists yet or this is not within the numerical tolerance of the previous value
			if (temp == null || !converged(temp, producerWeights[i.getOrder()])) {
				converged.set(false);
			}
		});
	}

	public void iterateAttractions(NetworkSkim skim) {

		converged.set(true);
		Collection<TravelSurveyZone> zones = purpose.getMarket().getZones().values();

		//For each attractor
		zones.parallelStream().forEach(j->{

			//Calculate a new denominator as sum_producers(productions*impedance*a)
			Double denom = zones.stream()
					.mapToDouble(x->
							(producerWeights[x.getOrder()] == null? 1.0 : producerWeights[x.getOrder()])
									*pa.getProductions(x)*friction.get(skim.getCost(x, j)))
					.sum();

			//Check for errors
			if (denom == 0.0 || denom.isNaN()) throw new RuntimeException();

			//Write a new B value for this attractor
			Double temp = attractorWeights[j.getOrder()];
			attractorWeights[j.getOrder()] = 1.0/denom;

			//If no B value exists yet or this is not within the numerical tolerance of the previous value
			if (temp == null || !converged(temp, attractorWeights[j.getOrder()])) {
				converged.set(false);
			}
		});

	}
	
	public AggregatePAMatrix getMatrix(NetworkSkim skim) {
		
		Collection<TravelSurveyZone> zones = purpose.getMarket().getZones().values();

		weights.updateWeights(producerWeights,attractorWeights);
		
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
						producerWeights[producer.getOrder()]
						*pa.getProductions(producer)
						*attractorWeights[attractor.getOrder()]
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
