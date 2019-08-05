package edu.utexas.wrap.demand;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;
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

	/**
	 * @return the graph to which this matrix is tied
	 */
	public Graph getGraph();

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
	public Float getDemand(TravelSurveyZone producer, TravelSurveyZone attractor);

	/**
	 * @param producer the Node from which trips are produced
	 * @return a map from all Attractions to the demand from this Node
	 */
	public DemandMap getDemandMap(TravelSurveyZone producer);

	/**
	 * @return the value of time of trips associated with this matrix
	 */
	public float getVOT();

	public void toFile(File out) throws IOException;
	
	public Collection<TravelSurveyZone> getProducers();

}
