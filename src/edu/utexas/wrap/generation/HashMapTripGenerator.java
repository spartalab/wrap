package edu.utexas.wrap.generation;

import java.util.Map;

import edu.utexas.wrap.demand.MarketSegment;
import edu.utexas.wrap.demand.containers.AggregatePAHashMap;
import edu.utexas.wrap.net.Graph;

public class HashMapTripGenerator extends TripGenerator {
	
	private Map<MarketSegment, Map<String, Float>> prodRates;
	private Map<MarketSegment, Map<String, Float>> attrRates; 
	private Graph graph;
	
	
	@Override
	public AggregatePAHashMap generate(MarketSegment segment) {
		Map<String,Float> segmentProdRates = prodRates.get(segment);
		Map<String,Float> segmentAttrRates = attrRates.get(segment);
		AggregatePAHashMap aggregate = new AggregatePAHashMap(graph);
		
		double prods = 0.0;
		double attrs = 0.0;
		
		for (Zone z : graph.getZones()) {
			for (String attribute : segmentProdRates.keySet()) {
				prods += prodRates.get(attribute)*z.valueOf(attribute);
			}
			for (String attribute : segmentAttrRates.keySet()) {
				attrs += attrRates.get(attribute)*z.valueOf(attribute);
			}
			aggregate.putProductions(z, prods);
			aggregate.putAttractions(z, attrs);
		}
		return aggregate;
	}

}
