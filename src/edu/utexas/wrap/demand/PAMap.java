package edu.utexas.wrap.demand;

import java.util.Collection;

import edu.utexas.wrap.net.TravelSurveyZone;

/**A mapping from a TravelSurveyZone to the number of productions
 * and attractions that begin and end, respectively,
 * at that TravelSurveyZone. This is the result of the trip 
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
	 * @return the TravelSurveyZones from which trips originate
	 */
	public Collection<TravelSurveyZone> getProducers();

	/**
	 * @return the TravelSurveyZones to which trips are attracted
	 */
	public Collection<TravelSurveyZone> getAttractors();

	/**
	 * @param z the TravelSurveyZone to which trips are attracted
	 * @return the number of trips attracted to the TravelSurveyZone
	 */
	public float getAttractions(TravelSurveyZone z);

	/**
	 * @param z the Node from which trips are produced
	 * @return the number of trips produced at the TravelSurveyZone
	 */
	public float getProductions(TravelSurveyZone z);

	/**
	 * @param z the TravelSurveyZone to which trips are attracted
	 * @param amt the amount of trips attracted to the TravelSurveyZone
	 */
	public void putAttractions(TravelSurveyZone z, Float amt);

	/**
	 * @param z the TravelSurveyZone from which trips are produced
	 * @param amt the amount of trips produced at the TravelSurveyZone
	 */
	public void putProductions(TravelSurveyZone z, Float amt);

	
	/**
	 * @return a DemandMap containing all trip productions of this map
	 */
	public DemandMap getProductionMap();
	
	/**
	 * @return a DemandMap containing all trip attractions of this map
	 */
	public DemandMap getAttractionMap();
}
