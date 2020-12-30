package edu.utexas.wrap.net;

/**Interface for defining a cost of travel between two zones
 * 
 * This interface provides a mechanism for defining a matrix of costs
 * between any two TravelSurveyZones in a network. That is, given any
 * two TSZs, each implementation of this interface provides some arbitrary
 * cost of travel between the two TSZs.
 * 
 * @author William
 *
 */
public interface NetworkSkim {

	public float getCost(TravelSurveyZone producer, TravelSurveyZone attractor);

}
