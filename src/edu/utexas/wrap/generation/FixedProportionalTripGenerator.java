package edu.utexas.wrap.generation;

import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.MarketSegment;
import edu.utexas.wrap.demand.PAMatrix;
import edu.utexas.wrap.demand.containers.AggregateFixedMultiplierPassthroughMatrix;

/**A secondary trip generator which uses a fixed rate
 * to generate trips across all TravelSurveyZones by
 * multiplying element-wise by a primary trip matrix.
 * This handles, generally speaking, non-home-based
 * trip generation.
 * 
 * For example, suppose a primary (home-based) trip
 * generation yielded the production-attraction matrix
 * [[0,1],[1,0]] and a proportion of 0.5 was used. The
 * resulting secondary trip production-attraction matrix
 * would be [[0,0.5],[0.5,0]].
 * 
 * @author William
 *
 */
public class FixedProportionalTripGenerator extends SecondaryTripGenerator {

	private final float rate;
	
	public FixedProportionalTripGenerator(float proportion) {
		rate = proportion;
	}
	
	@Override
	public PAMatrix generate(MarketSegment segment, AggregatePAMatrix homeBasedMatrix) {
		return new AggregateFixedMultiplierPassthroughMatrix(homeBasedMatrix,rate);
	}

}
