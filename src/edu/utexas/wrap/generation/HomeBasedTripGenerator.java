package edu.utexas.wrap.generation;

import edu.utexas.wrap.demand.MarketSegment;
import edu.utexas.wrap.demand.PAMap;

public abstract class HomeBasedTripGenerator {

	public abstract PAMap generate(MarketSegment segment);
	
}
