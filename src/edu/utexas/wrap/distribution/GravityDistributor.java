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


import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;


import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.demand.containers.FixedSizeAggregatePAMatrix;
import edu.utexas.wrap.demand.containers.FixedSizeDemandMap;
import edu.utexas.wrap.marketsegmentation.Purpose;
import edu.utexas.wrap.net.TravelSurveyZone;

/**A trip distribution class that uses the traditional gravity
 * model to calculate trips. Given a friction factor map, this
 * class calculates iteratively the balanced A and B values for
 * each TSZ, then multiplies each TSZ pair's productions and
 * attractions by the A and B values for the producer and
 * attractor, respectively, and the friction (impedance) between
 * these two zones to get the distributed trip matrix.
 * 
 * @author William
 *
 */
public class GravityDistributor implements TripDistributor {
	private final String id;
	private final int maxIterations;
	protected final Purpose purpose;
	private final Double margin = 0.001;
	private Double scalingFactor;
	private Collection<TravelSurveyZone> zones;
	protected Double[] producerWeights, attractorWeights;
	protected DistributionWeights weights;

	public GravityDistributor(String name, Purpose parent, Double scalingFactor, int maxIterations, DistributionWeights weights) {
		id = name;
		this.purpose = parent;
		this.scalingFactor = scalingFactor;
		this.maxIterations = maxIterations;
		this.weights = weights;
		producerWeights = weights.getProductionWeights();
		attractorWeights = weights.getAttractionWeights();
		zones = purpose.getMarket().getZones().values();
		
		
	}
	
	/**This method distributes a given PAMap according to the doubly-
	 * constrained gravity model. The method first iteratively calculates
	 * proportion values (A and B) for each zone, then determines the
	 * number of trips between any pair of zones as the product of the
	 * origin's number of productions, the origin's A value, the destination's
	 * number of attractions, the destination's B value, and the impedance
	 * between the two zones.
	 *
	 */
//	public AggregatePAMatrix distribute(PAMap pa,NetworkSkim skim) {
//		//Begin by iteratively calculating each zone's A and B values
//		ImpedanceMatrix impedances = new ImpedanceMatrix(zones, skim, friction);
//
//
//		AtomicBoolean converged = new AtomicBoolean(false);
//		//While the previous iteration made changes
//		int iterations = 0;
//		while (!converged.get() && iterations < 100) {
//			converged.set(true);
//			iterations++;
////			System.out.println("Iteration "+iterations);
//
//			updateProducerWeights(pa, impedances, converged);
//
//			
//			updateAttractorWeights(pa, impedances, converged);
//		}
//		
//		weights.updateWeights(producerWeights,attractorWeights);
//		
//		
//		return constructMatrix(pa, impedances);
//
//	}

	public AggregatePAMatrix constructMatrix(PAMap pa, ImpedanceMatrix impedances) {
		//Now begin constructing the matrix
		weights.updateWeights(producerWeights,attractorWeights);
		FixedSizeAggregatePAMatrix pam = new FixedSizeAggregatePAMatrix(zones);
		//For each producer
		zones.stream().forEach(producer -> {
			//Construct a new DemandMap
			DemandMap d = new FixedSizeDemandMap(zones);
			//For each attractor
			for (TravelSurveyZone attractor : zones) {
				//Calculate the number of trips between these two as a*productions*b*attractions*impedance
				Double Tij = 
						scalingFactor
						*producerWeights[producer.getOrder()]
						*pa.getProductions(producer)
						*attractorWeights[attractor.getOrder()]
						*pa.getAttractions(attractor)
						*impedances.getImpedance(producer, attractor);
				
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

	public void updateAttractorWeights(PAMap pa, ImpedanceMatrix impedances,
			AtomicBoolean converged) {
		//For each attractor
		zones.stream().forEach(j->{
			
			//Calculate a new denominator as sum_producers(productions*impedance*a)
			Double denom = zones.stream()
					.mapToDouble(x-> 
					(producerWeights[x.getOrder()] == null? 1.0 : producerWeights[x.getOrder()])
					*pa.getProductions(x)*impedances.getImpedance(x, j))
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

	public void updateProducerWeights(PAMap pa, ImpedanceMatrix impedances,
			AtomicBoolean converged) {
		//For each producer
		zones.stream().forEach(i -> {
			//Calculate a new denominator as sum_attractors(attractions*impedance*b)
			Double denom = zones.stream()
					.mapToDouble(x -> 
					(attractorWeights[x.getOrder()] == null? 1.0 : attractorWeights[x.getOrder()])
					*pa.getAttractions(x)*impedances.getImpedance(i, x))
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

	private Boolean converged(Double a, Double b) {
		return Math.abs(a-b) < margin;
	}
	
	@Override
	public String toString() {
		return id;
	}

	@Override
	public int maxIterations() {
		// TODO Auto-generated method stub
		return maxIterations;
	}
}
