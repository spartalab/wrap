package edu.utexas.wrap.distribution;

import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.PAMap;

/**The second step in the Urban Transportation Modeling System
 * a.k.a. the four-step model, trip distributors use some means to
 * convert a Production-Attraction map to a corresponding matrix
 * where the trips are linked to both a producer and an attractor.
 * The trips leaving each producer and entering each attractor should
 * be the same as the number of trips generated and stored in the 
 * given PA Map
 * @author William
 *
 */
public abstract class TripDistributor {

	public abstract AggregatePAMatrix distribute(PAMap pa);
	
}
