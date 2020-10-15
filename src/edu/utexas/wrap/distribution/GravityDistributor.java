package edu.utexas.wrap.distribution;


import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.demand.containers.AggregatePAHashMatrix;
import edu.utexas.wrap.demand.containers.FixedSizeDemandMap;
import edu.utexas.wrap.net.NetworkSkim;
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
public class GravityDistributor extends TripDistributor {
	private final NetworkSkim skim;
	private final FrictionFactorMap friction;
	private final Collection<TravelSurveyZone> zones;
	private final Double margin = 0.001;
//	private final Graph g;

	public GravityDistributor(Collection<TravelSurveyZone> zones, NetworkSkim skim, FrictionFactorMap fm) {
		this.zones = zones;
		friction = fm;
		this.skim = skim;
	}
	
	public AggregatePAMatrix distribute(PAMap pa) {
		//Begin by iteratively calculating each zone's A and B values

		Double[] a = new Double[zones.size()];
		Double[] b = new Double[zones.size()];
		
		Float[][] ff = new Float[zones.size()][zones.size()];
		
		zones.parallelStream().forEach(i -> {
			zones.stream().forEach(j ->{
				ff[i.getOrder()][j.getOrder()] = friction.get(skim.getCost(i, j));
			});
		});
		
		AtomicBoolean converged = new AtomicBoolean(false);
		//While the previous iteration made changes
		int iterations = 0;
		while (!converged.get() && iterations < 100) {
			converged.set(true);
			iterations++;
//			System.out.println("Iteration "+iterations);

			//For each producer
			zones.parallelStream().forEach(i -> {
				//Calculate a new denominator as sum_attractors(attractions*impedance*b)
				Double denom = zones.stream()
						.mapToDouble(x -> 
						(b[x.getOrder()] == null? 1.0 : b[x.getOrder()])
						*pa.getAttractions(x)*ff[i.getOrder()][x.getOrder()])
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

			
			//For each attractor
			zones.parallelStream().forEach(j->{
				
				//Calculate a new denominator as sum_producers(productions*impedance*a)
				Double denom = zones.stream()
						.mapToDouble(x-> 
						(a[x.getOrder()] == null? 1.0 : a[x.getOrder()])
						*pa.getProductions(x)*ff[x.getOrder()][j.getOrder()])
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
		

		
		
		//Now begin constructing the matrix
		AggregatePAHashMatrix pam = new AggregatePAHashMatrix(zones);
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
						*ff[producer.getOrder()][ attractor.getOrder()];
				
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

	private Boolean converged(Double a, Double b) {
		return Math.abs(a-b) < margin;
	}
}
