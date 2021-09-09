package edu.utexas.wrap.distribution;

import java.util.Collection;

import edu.utexas.wrap.net.NetworkSkim;
import edu.utexas.wrap.net.TravelSurveyZone;

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
