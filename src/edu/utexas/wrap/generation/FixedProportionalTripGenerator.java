package edu.utexas.wrap.generation;

import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.MarketSegment;
import edu.utexas.wrap.demand.PAMatrix;
import edu.utexas.wrap.demand.containers.AggregateFixedMultiplierPassthroughMatrix;
import edu.utexas.wrap.demand.containers.AggregatePAHashMatrix;

public class FixedProportionalTripGenerator extends NonHomeBasedTripGenerator {

	private final float rate;
	
	public FixedProportionalTripGenerator(float proportion) {
		rate = proportion;
	}
	
	@Override
	public PAMatrix generate(MarketSegment segment, AggregatePAMatrix homeBasedMatrix) {
		return new AggregateFixedMultiplierPassthroughMatrix(homeBasedMatrix,rate);
	}

}
