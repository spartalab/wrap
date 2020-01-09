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
import edu.utexas.wrap.generation.GenerationRate;
import edu.utexas.wrap.marketsegmentation.MarketSegment;

public interface TripPurpose {

	public Stream<Entry<MarketSegment, GenerationRate>> getProductionRates();
	
	public Stream<Entry<MarketSegment, GenerationRate>> getAttractionRates();
	
	public Stream<Entry<MarketSegment, DemandMap>> getProductions(Stream<Entry<MarketSegment,GenerationRate>> prodRates);
	
	public Stream<Entry<MarketSegment, DemandMap>> getAttractions(Stream<Entry<MarketSegment,GenerationRate>> attrRates);
	
	public Stream<Entry<MarketSegment, PAMap>> buildProductionAttractionMaps(
			Stream<Entry<MarketSegment, DemandMap>> prods,
			Stream<Entry<MarketSegment, DemandMap>> attrs);
	
	public Stream<Entry<MarketSegment, PAMap>> balance(Stream<Entry<MarketSegment,PAMap>> paMaps);
	
	public Stream<Entry<MarketSegment, AggregatePAMatrix>> distribute(Stream<Entry<MarketSegment,PAMap>> paMaps);
	
	public Stream<Entry<MarketSegment, Collection<ModalPAMatrix>>> modeChoice(Stream<Entry<MarketSegment,AggregatePAMatrix>> paMatrices);
	
	public Map<TimePeriod,Stream<Entry<MarketSegment, Collection<ODMatrix>>>> convertToOD(Stream<Entry<MarketSegment,Collection<ModalPAMatrix>>> paMatrices);
	
	public String toString();
}
