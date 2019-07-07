package edu.utexas.wrap.generation;

import java.util.HashMap;
import java.util.Map;

import edu.utexas.wrap.demand.MarketSegment;
import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.demand.PAMatrix;
import edu.utexas.wrap.demand.containers.AggregatePAHashMatrix;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;

public class NHBTripGenerator {
	
	private Map<MarketSegment,Map<TravelSurveyZone,Float>> rates;
	
	public NHBTripGenerator(Graph g, Map<MarketSegment,Float> hbwProdRates, Map<MarketSegment,Float> nhbwProdRates, Map<MarketSegment,PAMap> hbwTrips) {
		Map<TravelSurveyZone, Float> totalTrips = new HashMap<TravelSurveyZone,Float>();
		//Collect all home-based trip segments together to get sum-total production vector
		g.getTSZs().parallelStream().forEach(tsz -> {
			totalTrips.put(tsz, (float) hbwTrips.values().parallelStream().mapToDouble(x -> x.getProductions(tsz.getNode())).sum());
		});
		
		//Calculate each segment's share of the total trips
		Map<MarketSegment,Map<TravelSurveyZone,Float>> shares = new HashMap<MarketSegment,Map<TravelSurveyZone,Float>>(hbwTrips.size(),1.0f);
		hbwTrips.keySet().parallelStream().forEach(sgmt ->{
			Map<TravelSurveyZone,Float> share = new HashMap<TravelSurveyZone,Float>(g.numZones(),1.0f);
			g.getTSZs().parallelStream().forEach(tsz ->{
				share.put(tsz,hbwTrips.get(sgmt).getProductions(tsz.getNode())/totalTrips.get(tsz));
			});
			shares.put(sgmt, share);
		});
		
		//Calculate production rates
		hbwProdRates.keySet().parallelStream().forEach(sgmt -> {
			float factor = nhbwProdRates.get(sgmt)/hbwProdRates.get(sgmt);
			Map<TravelSurveyZone, Float> rate = new HashMap<TravelSurveyZone,Float>(g.numZones(),1.0f);
			Map<TravelSurveyZone,Float> share = shares.get(sgmt);
			share.keySet().parallelStream().forEach(zone -> {
				rate.put(zone, share.get(zone)*factor);
			});;
			rates.put(sgmt, rate);
		});
		
	}

	public PAMatrix generate(MarketSegment segment, PAMatrix hbwSum) {
		// TODO Auto-generated method stub
		return new AggregatePAHashMatrix(hbwSum,rates.get(segment));
	}

}
