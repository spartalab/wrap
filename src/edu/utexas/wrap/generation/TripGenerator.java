package edu.utexas.wrap.generation;


import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.net.Demographic;

/**An interface which defines the origin point of a
 * DemandMap, taking in a Demographic and returning
 * a number of trips produced or attracted to each
 * zone
 * 
 * @author William
 *
 */
public interface TripGenerator {
	
	public DemandMap generate(Demographic demographic);
}
