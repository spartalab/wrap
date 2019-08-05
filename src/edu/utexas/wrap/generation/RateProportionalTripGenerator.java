package edu.utexas.wrap.generation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.MarketSegment;
import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.demand.PAMatrix;
import edu.utexas.wrap.demand.containers.AggregatePAHashMatrix;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;

public class RateProportionalTripGenerator extends NonHomeBasedTripGenerator {
	
	private Map<MarketSegment,Map<TravelSurveyZone,Float>> rates;
	
	public RateProportionalTripGenerator(Graph g, 
			Map<MarketSegment,Float> homeBasedProductionRates, 
			Map<MarketSegment,Float> nonHomeBasedProductionRates, 
			Map<MarketSegment,PAMap> homeBasedTrips) {
		
		Map<MarketSegment, Map<TravelSurveyZone, Float>> shares = getTripShares(g, homeBasedTrips);
		
		calculateRelativeRates(g, homeBasedProductionRates, nonHomeBasedProductionRates, shares);
		
	}

	private void calculateRelativeRates(Graph g, 
			Map<MarketSegment, Float> homeBasedProductionRates,
			Map<MarketSegment, Float> nonHomeBasedProductionRates,
			Map<MarketSegment, Map<TravelSurveyZone, Float>> shares) {
		
		//Calculate relative production rates
		homeBasedProductionRates.keySet().parallelStream().forEach(sgmt -> {
			//Determine the ratio of NHB to HB trips
			if (homeBasedProductionRates.get(sgmt) <= 0)  return;
			
			float factor = nonHomeBasedProductionRates.get(sgmt)/homeBasedProductionRates.get(sgmt);
			Map<TravelSurveyZone, Float> rate = new HashMap<TravelSurveyZone,Float>(g.numZones(),1.0f);
			Map<TravelSurveyZone,Float> share = shares.get(sgmt);
			
			share.keySet().parallelStream().forEach(zone -> {
				rate.put(zone, share.get(zone)*factor);
			});
			
			rates.put(sgmt, rate);
		});
	}

	private Map<MarketSegment, Map<TravelSurveyZone, Float>> getTripShares(Graph g,
			Map<MarketSegment, PAMap> homeBasedTrips) {
		
		Map<TravelSurveyZone, Float> totalTrips = getTotalTrips(g, homeBasedTrips); 
		
		//Calculate each segment's household share of the total trips

		return homeBasedTrips.keySet().parallelStream().collect(
				//Map each segment to:
				Collectors.toMap(Function.identity(), 
					sgmt-> g.getTSZs().parallelStream().filter(tsz -> totalTrips.get(tsz) > 0).collect(
						//A Map from each zone with trips to
						Collectors.toMap(Function.identity(), 
							//A rate based on the segment's share of the total trips from that zone
							tsz -> homeBasedTrips.get(sgmt).getProductions(tsz)/totalTrips.get(tsz)
							)
						)
					)
				);
	}

	private Map<TravelSurveyZone, Float> getTotalTrips(Graph g, Map<MarketSegment, PAMap> homeBasedTrips) {
		//Collect all home-based trip segments together to get sum-total production vector
		return g.getTSZs().parallelStream().collect(
			Collectors.toMap(Function.identity(), 
				//Calculate the sum total productions across all input MarketSegments for this TSZ
				tsz -> (float) homeBasedTrips.values().parallelStream().mapToDouble(x -> x.getProductions(tsz)).sum()));
	}

	@Override
	public PAMatrix generate(MarketSegment segment, AggregatePAMatrix homeBasedTripMatrix) {
		return new AggregatePAHashMatrix(homeBasedTripMatrix,rates.get(segment));
	}

}
