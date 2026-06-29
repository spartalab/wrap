package edu.utexas.wrap.distribution;

import java.util.Collection;

import edu.utexas.wrap.net.NetworkSkim;
import edu.utexas.wrap.net.TravelSurveyZone;

/**
 * A pre-computed matrix of friction factor (impedance) values between all
 * zone pairs. Constructed from a {@link NetworkSkim} and a
 * {@link FrictionFactorMap}, this matrix caches the impedance values to
 * avoid repeated lookups during gravity model distribution iterations.
 *
 * @author Will Alexander
 * @see FrictionFactorMap
 * @see GravityDistributor
 */
public class ImpedanceMatrix {
	private Float[][] impedances;

	public ImpedanceMatrix(Collection<TravelSurveyZone> zones, NetworkSkim skim, FrictionFactorMap friction) {
		// TODO Auto-generated constructor stub
		impedances = new Float[zones.size()][zones.size()];
		zones.stream().forEach(i ->
			zones.stream().forEach(j ->	
			impedances[i.getOrder()][j.getOrder()] = friction.get(skim.getCost(i, j))));
			
		
	}

	public Float getImpedance(TravelSurveyZone producer, TravelSurveyZone attractor) {
		return impedances[producer.getOrder()][attractor.getOrder()];
	}
}
