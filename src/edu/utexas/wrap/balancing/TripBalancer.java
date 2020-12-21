package edu.utexas.wrap.balancing;

import edu.utexas.wrap.demand.PAMap;

/**An interface which takes a PAMap of arbitrary number of productions and attractions,
 * then returns a modified PAMap which guarantees that the number of productions is equal
 * to the number of attractions.
 * 
 * @author William
 *
 */
public interface TripBalancer {

	/**
	 * Balance Trips such that the total number of productions is equivalent to the total number of attractions
	 */
	public PAMap balance(PAMap paMap); //TODO don't balance in-place
}
