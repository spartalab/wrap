package edu.utexas.wrap.generation;

import edu.utexas.wrap.demand.MarketSegment;
import edu.utexas.wrap.demand.PAMap;

/**A trip generator which does not depend on
 * prior trips. This is generally used for home-
 * based trips and can depend on values from
 * various demographics.
 * 
 * @author William
 *
 */
public abstract class PrimaryTripGenerator {

	public abstract PAMap generate(MarketSegment segment);
	
}
