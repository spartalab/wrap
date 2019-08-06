package edu.utexas.wrap.distribution;

import java.util.Map;

import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.demand.containers.AggregatePAHashMatrix;
import edu.utexas.wrap.demand.containers.DemandHashMap;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;
import it.unimi.dsi.fastutil.objects.Object2DoubleMaps;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;

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
	private FrictionFactorMap friction;
	private Graph g;
	Boolean converged = false;

	public GravityDistributor(Graph g, FrictionFactorMap fm) {
		this.g = g;
		friction = fm;
	}
	
	@Override
	public AggregatePAMatrix distribute(PAMap pa) {
		//Begin by iteratively calculating each zone's A and B values
		Map<TravelSurveyZone, Double> a = Object2DoubleMaps.synchronize(new Object2DoubleOpenHashMap<TravelSurveyZone>(g.numZones(),1.0f));
		Map<TravelSurveyZone, Double> b = Object2DoubleMaps.synchronize(new Object2DoubleOpenHashMap<TravelSurveyZone>(g.numZones(),1.0f));

		//While the previous iteration made changes
		int iterations = 0;
		while (!converged && iterations < 100) {
			converged = true;
			iterations++;
//			System.out.println("Iteration "+iterations);
			
			//For each producer
			pa.getProducers().parallelStream().forEach(i -> {
				//Calculate a new denominator as sum_attractors(attractions*impedance*b)
				Double denom = pa.getAttractors().parallelStream()
						.mapToDouble(x -> b.getOrDefault(x, 1.0)*pa.getAttractions(x)*friction.get(i,x)).sum();
				//Check for errors
				if (denom == 0.0 || denom.isNaN()) throw new RuntimeException();
				//If no A value exists yet or this is not within the numerical tolerance of the previous value
				if (!a.containsKey(i) || !converged(a.getOrDefault(i,1.0), 1.0/denom)) {
					//Write a new A value for this producer
					converged = false;
					a.put(i, 1.0/denom);
				}
			});

			//For each attractor
			pa.getAttractors().forEach(j->{
				//Calculate a new denominator as sum_producers(productions*impedance*a)
				Double denom = pa.getProducers().parallelStream()
						.mapToDouble(x-> a.getOrDefault(x, 1.0)*pa.getProductions(x)*friction.get(x,j)).sum();
				//Check for errors
				if (denom == 0.0 || denom.isNaN()) throw new RuntimeException();
				//If no B value exists yet or this is not within the numerical tolerance of the previous value
				if (!b.containsKey(j) || !converged(b.getOrDefault(j,1.0), 1.0/denom)) {
					//Write a new B value for this attractor
					converged = false;
					b.put(j, 1.0/denom);
				}	
			});

		}
		
		//Now begin constructing the matrix
		AggregatePAHashMatrix pam = new AggregatePAHashMatrix(g);
		//For each producer
		pa.getProducers().parallelStream().forEach(producer -> {
			//Construct a new DemandMap
			DemandMap d = new DemandHashMap(g);
			//For each attractor
			for (TravelSurveyZone attractor : pa.getAttractors()) {
				//Calculate the number of trips between these two as a*productions*b*attractions*impedance
				Double Tij = a.get(producer)*pa.getProductions(producer)
						*b.get(attractor)*pa.getAttractions(attractor)
						*friction.get(producer, attractor);
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
		Double margin = 0.001;
		return Math.abs(a-b) < margin;
	}
}
