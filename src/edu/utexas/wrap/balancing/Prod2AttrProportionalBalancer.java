package edu.utexas.wrap.balancing;

import java.util.Set;

import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.net.RegionalAreaAnalysisZone;

/**This class balances PAMaps by calculating the total productions and
 * attractions, dividing the latter by the former, and multiplying each
 * TSZ's productions by that ratio. In this way, the total productions
 * are scaled up to match the total productions. 
 * 
 * If RegionalAreaAnalysisZones are provided, this is balanced on a
 * per-RAA basis; otherwise, the balancing is performed on a network-
 * wide basis
 * 
 * @author William
 *
 */
public class Prod2AttrProportionalBalancer implements TripBalancer {

	private Set<RegionalAreaAnalysisZone> raas;
	
	public Prod2AttrProportionalBalancer(Set<RegionalAreaAnalysisZone> regionalAnalysisZones) {
		raas = regionalAnalysisZones;
	}
	
	@Override
	public PAMap balance(PAMap paMap) {
		if (raas != null) raas.stream().forEach(raa -> {
			float prods = (float) raa.getTSZs().stream().mapToDouble(tsz ->paMap.getProductions(tsz)).sum();
			float attrs = (float) raa.getTSZs().stream().mapToDouble(tsz -> paMap.getAttractions(tsz)).sum();

			float prop = attrs/prods;
			raa.getTSZs().stream().forEach(tsz -> paMap.putProductions(tsz, paMap.getProductions(tsz)*prop));
		});
		else {
			double prods = paMap.getProducers().stream().mapToDouble(prod -> paMap.getProductions(prod)).sum();
			double attrs = paMap.getAttractors().stream().mapToDouble(attr -> paMap.getAttractions(attr)).sum();
			
			double scale = attrs/prods;
			paMap.getProducers().stream().forEach(prod -> paMap.putProductions(prod, (float) (paMap.getProductions(prod)*scale)));
		}
		return paMap;
	}

}
