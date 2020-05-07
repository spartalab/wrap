package edu.utexas.wrap.generation;


import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.marketsegmentation.MarketSubsegment;

public interface TripGenerator {
	
	public DemandMap generate(MarketSubsegment segment);
}
