package edu.utexas.wrap.demand;

import java.util.Collection;

import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;

/**A mapping from a zone to the number of productions
 * and attractions that begin and end, respectively,
 * at that zone. This is the result of the trip 
 * generation step of the four-step model.
 * 
 * Any PA Map should have a method of retrieving the
 * metadata that will be used in the mode-choice models.
 * 
 * @author William
 *
 */
public interface PAMap {

	/**
	 * @return the Nodes from which trips originate
	 */
	public Collection<TravelSurveyZone> getProducers();

	/**
	 * @return the Nodes to which trips are attracted
	 */
	public Collection<TravelSurveyZone> getAttractors();

	/**
	 * @param z the Node to which trips are attracted
	 * @return the number of trips attracted to the Node
	 */
	public Float getAttractions(TravelSurveyZone z);

	/**
	 * @param z the Node from which trips are produced
	 * @return the number of trips produced at the Node
	 */
	public Float getProductions(TravelSurveyZone z);

	/**
	 * @return the Graph to which the PA map is tied
	 */
	public Graph getGraph();

	/**
	 * @param z the Node to which trips are attracted
	 * @param amt the amount of trips attracted to the Node
	 */
	public void putAttractions(TravelSurveyZone z, Float amt);

	/**
	 * @param z the Node from which trips are produced
	 * @param amt the amount of trips produced at the Node
	 */
	public void putProductions(TravelSurveyZone z, Float amt);

	/**
	 * @return the value of time of trips associated with this matrix
	 */
	public Float getVOT();
}
