package edu.utexas.wrap.generation;


import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.marketsegmentation.MarketSegment;

public interface TripGenerator {
	
	public DemandMap generate(MarketSegment segment);
}
