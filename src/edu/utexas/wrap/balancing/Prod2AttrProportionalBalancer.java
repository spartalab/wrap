package edu.utexas.wrap.balancing;

import java.util.Set;

import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.net.RegionalAreaAnalysisZone;

public class Prod2AttrProportionalBalancer implements TripBalancer {

	private Set<RegionalAreaAnalysisZone> raas;
	
	public Prod2AttrProportionalBalancer(Set<RegionalAreaAnalysisZone> regionalAnalysisZones) {
		raas = regionalAnalysisZones;
	}
	
	@Override
	public void balance(PAMap paMap) {
		if (raas != null) raas.parallelStream().forEach(raa -> {
			float prods = (float) raa.getTSZs().parallelStream().mapToDouble(tsz ->paMap.getProductions(tsz)).sum();
			float attrs = (float) raa.getTSZs().parallelStream().mapToDouble(tsz -> paMap.getAttractions(tsz)).sum();

			float prop = attrs/prods;
			raa.getTSZs().parallelStream().forEach(tsz -> paMap.putProductions(tsz, paMap.getProductions(tsz)*prop));
		});
		else {
			double prods = paMap.getProducers().parallelStream().mapToDouble(prod -> paMap.getProductions(prod)).sum();
			double attrs = paMap.getAttractors().parallelStream().mapToDouble(attr -> paMap.getAttractions(attr)).sum();
			
			double scale = attrs/prods;
			paMap.getProducers().parallelStream().forEach(prod -> paMap.putProductions(prod, (float) (paMap.getProductions(prod)*scale)));
		}
	}

}
