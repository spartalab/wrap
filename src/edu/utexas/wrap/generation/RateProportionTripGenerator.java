package edu.utexas.wrap.generation;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
public class RateProportionTripGenerator implements TripGenerator {
	
	private Map<TravelSurveyZone, Double> totalProds;
	Map<MarketSegment,Map<TravelSurveyZone,Double>> shares;
	Map<MarketSegment,Map<TravelSurveyZone,Double>> rates;
	Map<MarketSegment,DemandMap> primaryMaps;
	
	public RateProportionTripGenerator(Graph g, 
			Map<MarketSegment,GenerationRate> primaryProductionRates, 
			Map<MarketSegment,GenerationRate> secondaryProductionRates, 
			Stream<Entry<MarketSegment,DemandMap>> primaryStream) {
		
		primaryMaps = primaryStream.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
		
		totalProds = g.getTSZs().parallelStream().collect(Collectors.toMap(Function.identity(), 
				tsz -> primaryMaps.values().parallelStream()
				.mapToDouble(
						map -> map.get(tsz)
						).sum()
				));
		
		shares = getTripShares(g, primaryMaps);
		
		calculateRelativeRates(g, primaryProductionRates, secondaryProductionRates);
		
	}

	private void calculateRelativeRates(Graph g, 
			Map<MarketSegment, GenerationRate> primaryProductionRates,
			Map<MarketSegment, GenerationRate> secondaryProductionRates) {
		
		//Calculate relative production rates

		rates = shares.entrySet().parallelStream().collect(
				Collectors.toMap(Entry::getKey, 
						segEntry -> 
									

							 segEntry.getValue().entrySet().parallelStream().collect(
									Collectors.toMap(Entry::getKey, tszEntry ->
									tszEntry.getValue()*secondaryProductionRates.entrySet().parallelStream().filter(
											secondaryEntry -> 
											compareByLooserMS(segEntry.getKey(),secondaryEntry.getKey())
											)
									.mapToDouble(secondaryRate -> 

										secondaryRate.getValue().getRate(tszEntry.getKey()) 
											/ primaryProductionRates.entrySet().parallelStream()
										.filter(primaryEntry ->
										compareByLooserMS(segEntry.getKey(), primaryEntry.getKey())
												)
										.mapToDouble(entry-> entry.getValue().getRate(tszEntry.getKey())).sum()

											)
									.sum()
											)
									 )
						
						)
				);
	}

	private boolean compareByLooserMS(MarketSegment stricterEntry,
			MarketSegment looserEntry) {
		return Stream.of(looserEntry.getClass().getInterfaces())
		.filter(ifc -> MarketSegment.class.isInstance(ifc) && ifc.isInstance(stricterEntry))
		.allMatch(ifc ->
				ifc.cast(stricterEntry).equals(ifc.cast(looserEntry))
				);
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

	public DemandMap generate(MarketSegment segment) {
		DemandMap demandMap = primaryMaps.get(segment);
		
		Map<TravelSurveyZone, Double> rate = rates.get(segment);
		DemandMap ret = new FixedSizeDemandMap(demandMap.getGraph());
		demandMap.getZones().parallelStream().forEach( 
				entry -> ret.put(entry, demandMap.get(entry)
						*rate.getOrDefault(entry,0.0))
		);
		return ret;
	}
}
