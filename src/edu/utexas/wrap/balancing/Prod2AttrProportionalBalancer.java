package edu.utexas.wrap.balancing;

import java.util.Set;

import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.net.RegionalAreaAnalysisZone;

public class Prod2AttrProportionalBalancer implements TripBalancer {

	private Set<RegionalAreaAnalysisZone> raas;
	
	@Override
	public void balance(PAMap paMap) {
		// TODO Auto-generated method stub
		raas.parallelStream().forEach(raa -> {
			float prods = (float) raa.getTSZs().parallelStream().mapToDouble(tsz ->paMap.getProductions(tsz)).sum();
			float attrs = (float) raa.getTSZs().parallelStream().mapToDouble(tsz -> paMap.getAttractions(tsz)).sum();

			float prop = attrs/prods;
			raa.getTSZs().parallelStream().forEach(tsz -> paMap.putProductions(tsz, paMap.getProductions(tsz)*prop));
		});
	}

}
