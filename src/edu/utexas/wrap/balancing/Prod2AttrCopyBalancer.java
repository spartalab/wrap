package edu.utexas.wrap.balancing;

import java.util.Collection;

import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.net.TravelSurveyZone;

/** This class balances PAMaps by replacing all zones' productions
 * with the number of attractions from the same zone.
 * 
 * @author William
 *
 */
public class Prod2AttrCopyBalancer implements TripBalancer {

	@Override
	public PAMap balance(PAMap paMap) {
		Collection<TravelSurveyZone> nodes = paMap.getAttractors();
		nodes.addAll(paMap.getProducers());
		nodes.stream().forEach(n -> paMap.putProductions(n, paMap.getAttractions(n)));
		return paMap;
	}

}
