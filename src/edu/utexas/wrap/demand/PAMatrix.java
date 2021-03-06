package edu.utexas.wrap.demand;

import java.util.Collection;

import edu.utexas.wrap.net.TravelSurveyZone;

/**A map from an origin and destination zone to the
 * number of trips between them. This may or may not
 * have a Mode associated with it. This is the output
 * of the trip distribution process (as an aggregate
 * matrix) and the mode choice process (as a modal
 * matrix)
 * 
 * Any PAMatrix should have the ability to retrieve
 * metadata which may be used in a trip-interchange
 * mode choice model.
 * 
 * @author William
 *
 */
public interface PAMatrix {

	public default Collection<TravelSurveyZone> getZones(){
		throw new RuntimeException("Not yet implemented");
	}

	/**
	 * @param producer the Node from which trips are produced
	 * @param attractor the Node to which trips are attracted
	 * @param demand the amount of trips between the produer and attractor
	 */
	public void put(TravelSurveyZone producer, TravelSurveyZone attractor, Float demand);

	/** Retrieve the demand between two points
	 * @param producer the Node producing trips
	 * @param attractor the Node attracting trips
	 * @return the number of trips from producer to attractor
	 */
	public float getDemand(TravelSurveyZone producer, TravelSurveyZone attractor);

	/**
	 * @param producer the Node from which trips are produced
	 * @return a map from all Attractions to the demand from this Node
	 */
	public DemandMap getDemandMap(TravelSurveyZone producer);

	/**
	 * @return a Collection of TravelSurveyZones which produce trips
	 */
	public Collection<TravelSurveyZone> getProducers();

}
