package edu.utexas.wrap.generation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.Combiner;
import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.demand.PAMatrix;
import edu.utexas.wrap.demand.containers.AggregatePAHashMatrix;
import edu.utexas.wrap.marketsegmentation.MarketSegment;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;

/**A secondary trip generator which calculates, for several
 * market segments, a per-zone rate to multiply a given
 * primary trip matrix by to get secondary trips. This is
 * done by calculating the total trips in each zone, then
 * getting each segment's share of the total trips. A factor
 * is calculated as the ratio between a given secondary rate
 * for this market segment and its given primary rate. This
 * factor is then multiplied by the trip share to get the 
 * rate which the primary trip matrix is multiplied by.
 * 
 * Calls to the generate method then return a pass-through
 * matrix dependent on the initial matrix.
 * 
 * 
 * @author William
 *
 */
public class RateProportionTripGenerator {
	
	private Map<TravelSurveyZone,Float> totalProds;
	Map<MarketSegment,Map<TravelSurveyZone,Float>> shares;
	
	public RateProportionTripGenerator(Graph g, 
			Map<MarketSegment,Float> primaryProductionRates, 
			Map<MarketSegment,Float> secondaryProductionRates, 
			Map<MarketSegment,PAMap> primaryTrips) {
		
		totalProds = Combiner.totalAttractions(g, primaryTrips.values());
		
		shares = getTripShares(g, primaryTrips);
		
		calculateRelativeRates(g, primaryProductionRates, secondaryProductionRates, shares);
		
	}

	private void calculateRelativeRates(Graph g, 
			Map<MarketSegment, Float> primaryProductionRates,
			Map<MarketSegment, Float> secondaryProductionRates,
			Map<MarketSegment, Map<TravelSurveyZone, Float>> shares) {
		
		//Calculate relative production rates
		primaryProductionRates.keySet().parallelStream().forEach(sgmt -> {
			//Determine the ratio of NHB to HB trips
			if (primaryProductionRates.get(sgmt) <= 0)  return;
			
			float factor = secondaryProductionRates.get(sgmt)/primaryProductionRates.get(sgmt);
			Map<TravelSurveyZone,Float> share = shares.get(sgmt);
			
			
		});
	}

	private Map<TravelSurveyZone, Float> getRates(Graph g, float factor, Map<TravelSurveyZone, Float> share) {
		Map<TravelSurveyZone, Float> rate = new HashMap<TravelSurveyZone,Float>(g.numZones(),1.0f);
		
		share.keySet().parallelStream().forEach(zone -> {
			rate.put(zone, share.get(zone)*factor);
		});
		return rate;
	}

	private Map<MarketSegment, Map<TravelSurveyZone, Float>> getTripShares(Graph g,
			Map<MarketSegment, PAMap> primaryTrips) {
		
				
//		getTotalTrips(g, primaryTrips); 
		
		//Calculate each segment's household share of the total trips

		return primaryTrips.keySet().parallelStream().collect(
				//Map each segment to:
				Collectors.toMap(Function.identity(), 
					sgmt-> g.getTSZs().parallelStream().filter(tsz -> totalProds.get(tsz) > 0).collect(
						//A Map from each zone with trips to
						Collectors.toMap(Function.identity(), 
							//A rate based on the segment's share of the total trips from that zone
							tsz -> primaryTrips.get(sgmt).getProductions(tsz)/totalProds.get(tsz)
							)
						)
					)
				);
	}

	public PAMatrix generate(Graph g, float factor, AggregatePAMatrix primaryTripMatrix, MarketSegment segment) {
		
		Map<TravelSurveyZone, Float> rate = getRates(g, factor, shares.get(segment));

		return new AggregatePAHashMatrix(primaryTripMatrix,rate);
	}
	
	public PAMatrix generate(Map<TravelSurveyZone, Float> perZoneRate, AggregatePAMatrix primaryMatrix) {
		return new AggregatePAHashMatrix(primaryMatrix, perZoneRate);
	}

}
