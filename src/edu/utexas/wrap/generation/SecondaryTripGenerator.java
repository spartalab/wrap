package edu.utexas.wrap.generation;

import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.MarketSegment;
import edu.utexas.wrap.demand.PAMatrix;

/**A class of trip generation which depends on prior
 * trips to determine the new production-attraction
 * matrix. This is generally used for non-home-based
 * trip generation, where the trips are dependent on
 * a trip matrix from home-based generation.
 * 
 * @author William
 *
 */
public abstract class SecondaryTripGenerator {

	public abstract PAMatrix generate(MarketSegment segment, AggregatePAMatrix homeBasedMatrix);

}
