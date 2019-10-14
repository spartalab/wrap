package edu.utexas.wrap.generation;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.containers.FixedSizeDemandMap;
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
	
	private Map<TravelSurveyZone, Double> totalProds;
	Map<MarketSegment,Map<TravelSurveyZone,Double>> shares;
	Map<MarketSegment,Map<TravelSurveyZone,Double>> rates;
	
	public RateProportionTripGenerator(Graph g, 
			Map<MarketSegment,Double> primaryProductionRates, 
			Map<MarketSegment,Double> secondaryProductionRates, 
			Map<MarketSegment, DemandMap> primaryProds) {
		
		totalProds = g.getTSZs().parallelStream().collect(Collectors.toMap(Function.identity(), 
				tsz -> primaryProds.values().parallelStream().mapToDouble(map -> map.getOrDefault(tsz,0.0)).sum()
				));
		
		shares = getTripShares(g, primaryProds);
		
		calculateRelativeRates(g, primaryProductionRates, secondaryProductionRates, shares);
		
	}

	private void calculateRelativeRates(Graph g, 
			Map<MarketSegment, Double> primaryProductionRates,
			Map<MarketSegment, Double> secondaryProductionRates,
			Map<MarketSegment, Map<TravelSurveyZone, Double>> shares) {
		
		//Calculate relative production rates
		rates = primaryProductionRates.keySet().parallelStream().collect( Collectors.toMap(Function.identity(), sgmt -> {
			//Determine the ratio of NHB to HB trips
			if (primaryProductionRates.get(sgmt) <= 0)  return null;
			
			double factor = secondaryProductionRates.get(sgmt)/primaryProductionRates.get(sgmt);
			return shares.get(sgmt).entrySet().parallelStream().collect(Collectors.toMap(
					Entry::getKey,
					entry -> entry.getValue()*factor
					));			
		}));
	}

	private Map<MarketSegment, Map<TravelSurveyZone,Double>> getTripShares(Graph g,
			Map<MarketSegment, DemandMap> primaryProds) {
				
		//Calculate each segment's household share of the total trips

		return primaryProds.keySet().parallelStream().collect(
				//Map each segment to:
				Collectors.toMap(Function.identity(), 
					sgmt-> g.getTSZs().parallelStream().filter(tsz -> totalProds.get(tsz) > 0).collect(
						//A Map from each zone with trips to
						Collectors.toMap(Function.identity(), 
							//A rate based on the segment's share of the total trips from that zone
							tsz -> primaryProds.get(sgmt).get(tsz)/totalProds.get(tsz)
							)
						)
					)
				);
	}

	public DemandMap generate(DemandMap demandMap, MarketSegment segment) {
		
		Map<TravelSurveyZone, Double> rate = rates.get(segment);
		DemandMap ret = new FixedSizeDemandMap(demandMap.getGraph());
		demandMap.getZones().parallelStream().forEach( 
				entry -> ret.put(entry, demandMap.get(entry)*rate.get(entry))
		);
		return ret;
	}
}
