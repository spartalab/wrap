package edu.utexas.wrap;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.ModalPAMatrix;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.marketsegmentation.MarketSegment;

public interface TripPurpose {

	public Stream<Entry<MarketSegment, DemandMap>> getProductions();
	
	public Stream<Entry<MarketSegment, DemandMap>> getAttractions();
	
	public Stream<Entry<MarketSegment, PAMap>> buildProductionAttractionMaps(
			Stream<Entry<MarketSegment, DemandMap>> prods,
			Stream<Entry<MarketSegment, DemandMap>> attrs);
	
	public Stream<Entry<MarketSegment, PAMap>> balance(
			Stream<Entry<MarketSegment,PAMap>> paMaps);
	
	public Stream<Entry<MarketSegment, AggregatePAMatrix>> distribute(
			Stream<Entry<MarketSegment,PAMap>> paMaps);
	
	public Stream<Entry<MarketSegment, Collection<ModalPAMatrix>>> modeChoice(
			Stream<Entry<MarketSegment,AggregatePAMatrix>> paMatrices);
	
	public Stream<Entry<MarketSegment, Map<TimePeriod, Collection<ODMatrix>>>> convertToOD(
			Stream<Entry<MarketSegment,Collection<ModalPAMatrix>>> paMatrices);
	
	public String toString();

	public Map<MarketSegment,Collection<ODMatrix>> getODMap(TimePeriod tp);
}
