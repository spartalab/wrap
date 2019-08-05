package edu.utexas.wrap.generation;

import java.util.Map;

import edu.utexas.wrap.demand.MarketSegment;
import edu.utexas.wrap.demand.containers.AggregatePAHashMap;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;

public class HashMapTripGenerator extends HomeBasedTripGenerator {

	private Map<MarketSegment, Map<String, Float>> prodRates;
	private Map<MarketSegment, Map<String, Float>> attrRates; 
	private Graph graph;


	@Override
	public AggregatePAHashMap generate(MarketSegment segment) {
		Map<String,Float> segmentProdRates = prodRates.get(segment);
		Map<String,Float> segmentAttrRates = attrRates.get(segment);
		AggregatePAHashMap aggregate = new AggregatePAHashMap(graph);


		graph.getTSZs().parallelStream().forEach(z ->{

			float prods = (float) segmentProdRates.entrySet().parallelStream().mapToDouble(x -> x.getValue()*z.valueOf(x.getKey())).sum();
			float attrs = (float) segmentAttrRates.entrySet().parallelStream().mapToDouble(x -> x.getValue()*z.valueOf(x.getKey())).sum();

			aggregate.putProductions(z, prods);
			aggregate.putAttractions(z, attrs);
		});
		return aggregate;
	}

}
