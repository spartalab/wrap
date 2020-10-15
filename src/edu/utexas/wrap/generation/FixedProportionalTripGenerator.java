package edu.utexas.wrap.generation;

import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.PAMatrix;
import edu.utexas.wrap.demand.containers.FixedMultiplierPassthroughAggregateMatrix;
import edu.utexas.wrap.marketsegmentation.MarketSegment;

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
public class FixedProportionalTripGenerator {

	private final float rate;
	
	public FixedProportionalTripGenerator(float proportion) {
		rate = proportion;
	}
	
	public PAMatrix generate(MarketSegment segment, AggregatePAMatrix homeBasedMatrix) {
		return new FixedMultiplierPassthroughAggregateMatrix(homeBasedMatrix,rate);
	}

}
