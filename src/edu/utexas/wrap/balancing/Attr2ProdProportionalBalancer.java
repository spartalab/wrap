package edu.utexas.wrap.balancing;

import java.util.Set;

import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.net.RegionalAreaAnalysisZone;

/** This class balances PAMaps by calculating the total productions and
 * attractions, dividing the former by the latter, and multiplying each
 * TSZ's attractions by that ratio. In this way, the total attractions
 * are scaled up to match the total productions.
 * 
 * If RegionalAreaAnalysisZones are provided, this is balanced on a
 * per-RAA basis; otherwise, the balancing is performed on a network-
 * wide basis
 * 
 * @author William
 *
 */
public class Attr2ProdProportionalBalancer implements TripBalancer {

	private Set<RegionalAreaAnalysisZone> raas;
	
	@Override
	public void balance(PAMap paMap) {
		if (raas != null) raas.parallelStream().forEach(raa -> {
			float prods = (float) raa.getTSZs().parallelStream().mapToDouble(tsz ->paMap.getProductions(tsz)).sum();
			float attrs = (float) raa.getTSZs().parallelStream().mapToDouble(tsz -> paMap.getAttractions(tsz)).sum();

			float prop = prods/attrs;
			raa.getTSZs().parallelStream().forEach(tsz -> paMap.putAttractions(tsz, paMap.getAttractions(tsz)*prop));
		});
		else {
			double prods = paMap.getProducers().parallelStream().mapToDouble(prod -> paMap.getProductions(prod)).sum();
			double attrs = paMap.getAttractors().parallelStream().mapToDouble(attr -> paMap.getAttractions(attr)).sum();
			
			double scale = prods/attrs;
			paMap.getAttractors().parallelStream().forEach(attr -> paMap.putAttractions(attr, (float) (paMap.getAttractions(attr)*scale)));
		}
		
	}

}
