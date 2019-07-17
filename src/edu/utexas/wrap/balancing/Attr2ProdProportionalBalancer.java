package edu.utexas.wrap.balancing;

import java.util.Set;

import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.net.RegionalAreaAnalysisZone;

public class Attr2ProdProportionalBalancer implements TripBalancer {

	private Set<RegionalAreaAnalysisZone> raas;
	
	@Override
	public void balance(PAMap paMap) {
		// TODO Auto-generated method stub
		raas.parallelStream().forEach(raa -> {
			float prods = (float) raa.getTSZs().parallelStream().mapToDouble(tsz ->paMap.getProductions(tsz.getNode())).sum();
			float attrs = (float) raa.getTSZs().parallelStream().mapToDouble(tsz -> paMap.getAttractions(tsz.getNode())).sum();

			float prop = prods/attrs;
			raa.getTSZs().parallelStream().forEach(tsz -> paMap.putAttractions(tsz.getNode(), paMap.getAttractions(tsz.getNode())*prop));
		});
		
	}

}