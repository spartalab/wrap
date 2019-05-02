package edu.utexas.wrap.generation;

import edu.utexas.wrap.demand.MarketSegment;
import edu.utexas.wrap.demand.PAMap;

public abstract class TripGenerator {

	public abstract PAMap generate(MarketSegment segment);
}
