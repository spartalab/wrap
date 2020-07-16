package edu.utexas.wrap.generation;

import java.util.Collection;
import java.util.stream.IntStream;

import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.containers.FixedSizeDemandMap;
import edu.utexas.wrap.net.Demographic;
import edu.utexas.wrap.net.TravelSurveyZone;

public class BasicTripGenerator implements TripGenerator {
//	protected Graph g;
	private Collection<TravelSurveyZone> zones;
	private GenerationRate[] rates;
	
	public BasicTripGenerator(Collection<TravelSurveyZone> zones, GenerationRate[] generationRates) {
//		g = graph;
		this.zones = zones;
		rates = generationRates;
	}
	
	public DemandMap generate(Demographic demographic){
		DemandMap ret = new FixedSizeDemandMap(zones);

		zones.stream()
		.forEach(tsz -> {
			ret.put(tsz, 
					(float) IntStream.range(0, rates.length)
					.mapToDouble(
							category -> rates[category].getRate(tsz) * demographic.valueFor(tsz)[category]
							)
					.sum()
					);
		});
		
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

