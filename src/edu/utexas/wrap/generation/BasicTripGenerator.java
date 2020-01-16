package edu.utexas.wrap.generation;

import java.util.Map;

import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.containers.FixedSizeDemandMap;
import edu.utexas.wrap.marketsegmentation.MarketSegment;
import edu.utexas.wrap.net.AreaClass;
import edu.utexas.wrap.net.Graph;

public class BasicTripGenerator implements TripGenerator {
	protected Graph g;
	private Map<MarketSegment, GenerationRate> rates;
	
	public BasicTripGenerator(Graph graph, Map<MarketSegment,GenerationRate> rateMap) {
		g = graph;
		rates = rateMap;
	}
	
	public DemandMap generate(MarketSegment segment){
		GenerationRate rate = rates.get(segment);
		DemandMap ret = new FixedSizeDemandMap(g);
		g.getTSZs().parallelStream().forEach(	//For each TSZ in parallel,
				 tsz ->	//the TSZ maps to a value:
					//The data rate for this market segment times the market segment's value for this TSZ
						ret.put(tsz, (float) (rate.getRate(tsz)*segment.attributeDataGetter().applyAsDouble(tsz))));
		return ret;
	}
	
//	private DemandMap scale(DemandMap input, Map<AreaClass,Double> areaData){
//		DemandMap ret = new FixedSizeDemandMap(g);
//		input.getZones().parallelStream().forEach( tsz->{	//For each input key-value mapping in parallel,
//			ret.put(tsz, input.get(tsz)*areaData.get(tsz.getAreaClass()));
//				});
//		return ret;
//	}
//	
//	public DemandMap generateAndScale(MarketSegment segment, Map<AreaClass,Double> areaData){
//		return scale(generate(segment),areaData);
//	}
}

