package edu.utexas.wrap.balancing;

import java.util.Set;

import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.net.TravelSurveyZone;

public class Prod2AttrCopyBalancer implements TripBalancer {

	@Override
	public void balance(PAMap paMap) {
		Set<TravelSurveyZone> nodes = paMap.getAttractors();
		nodes.addAll(paMap.getProducers());
		nodes.parallelStream().forEach(n -> paMap.putProductions(n, paMap.getAttractions(n)));
	}

}
