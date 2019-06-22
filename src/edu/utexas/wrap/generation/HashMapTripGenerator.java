package edu.utexas.wrap.generation;

import edu.utexas.wrap.demand.MarketSegment;
import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.demand.containers.AggregatePAHashMap;

public abstract class HashMapTripGenerator extends TripGenerator {

	
	@Override
	public abstract AggregatePAHashMap generate(MarketSegment segment);

}
