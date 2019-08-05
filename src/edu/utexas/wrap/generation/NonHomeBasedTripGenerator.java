package edu.utexas.wrap.generation;

import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.MarketSegment;
import edu.utexas.wrap.demand.PAMatrix;

public abstract class NonHomeBasedTripGenerator {

	public abstract PAMatrix generate(MarketSegment segment, AggregatePAMatrix homeBasedMatrix);

}
