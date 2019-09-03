package edu.utexas.wrap.generation;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import edu.utexas.wrap.marketsegmentation.MarketSegment;
import edu.utexas.wrap.net.AreaClass;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;

public class AreaSpecificTripGenerator extends BasicTripGenerator {

	private Map<MarketSegment,Map<AreaClass,Double>> areaRates;
	
	public AreaSpecificTripGenerator(Graph graph, Map<MarketSegment, Map<AreaClass,Double>> rateMap) {
		super(graph, null);
		areaRates = rateMap;
	}
	
	@Override
	public Map<TravelSurveyZone, Double> generate(MarketSegment segment){
		return g.getTSZs().parallelStream().collect(
				Collectors.toMap(Function.identity(), tsz ->
				areaRates.get(segment).get(tsz.getAreaClass())*segment.attributeDataGetter().applyAsDouble(tsz)));
		
	}

}
