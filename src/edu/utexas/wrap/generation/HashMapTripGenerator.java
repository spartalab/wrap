package edu.utexas.wrap.generation;

import java.util.Map;

import edu.utexas.wrap.demand.MarketSegment;
import edu.utexas.wrap.demand.containers.AggregatePAHashMap;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;

public class HashMapTripGenerator extends TripGenerator {
	
	private Map<MarketSegment, Map<String, Float>> prodRates;
	private Map<MarketSegment, Map<String, Float>> attrRates; 
	private Graph graph;
	
	
	@Override
	public AggregatePAHashMap generate(MarketSegment segment) {
		Map<String,Float> segmentProdRates = prodRates.get(segment);
		Map<String,Float> segmentAttrRates = attrRates.get(segment);
		AggregatePAHashMap aggregate = new AggregatePAHashMap(graph);
		
		float prods = 0.0f;
		float attrs = 0.0f;
		
		for (TravelSurveyZone z : graph.getTSZs()) {
			for (String attribute : segmentProdRates.keySet()) {
				prods += segmentProdRates.get(attribute)*z.valueOf(attribute);
			}
			for (String attribute : segmentAttrRates.keySet()) {
				attrs += segmentAttrRates.get(attribute)*z.valueOf(attribute);
			}
			aggregate.putProductions(z.getNode(), prods);
			aggregate.putAttractions(z.getNode(), attrs);
		}
		return aggregate;
	}

}
