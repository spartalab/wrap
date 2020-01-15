package edu.utexas.wrap.generation;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.containers.FixedSizeDemandMap;
import edu.utexas.wrap.marketsegmentation.MarketSegment;
import edu.utexas.wrap.net.AreaClass;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;

@Deprecated //Use BasicTripGenerator with AreaClassGenerationRate instead
public class AreaSpecificTripGenerator extends BasicTripGenerator {

	private Map<MarketSegment,Map<AreaClass,Double>> areaRates;
	
	public AreaSpecificTripGenerator(Graph graph, Map<MarketSegment, Map<AreaClass,Double>> rateMap) {
		super(graph, null);
		areaRates = rateMap;
	}
	
	@Override
	public DemandMap generate(MarketSegment segment){
		DemandMap ret = new FixedSizeDemandMap(g);
		g.getTSZs().parallelStream().forEach( tsz->
				ret.put(tsz, areaRates.get(segment).get(tsz.getAreaClass())*segment.attributeDataGetter().applyAsDouble(tsz)));
		return ret;
	}

}
