package edu.utexas.wrap.demand;

import java.util.Collection;

import edu.utexas.wrap.TimePeriod;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.TravelSurveyZone;

/**A map from an origin-destination pair to the number
 * of <b>vehicle</b>-trips made. This is different from the PA
 * matrix in that it may account for multiple person-trips
 * inside a single vehicle-trip, i.e. passengers in a car
 * or bus.
 * 
 * @author William
 *
 */
public interface ODMatrix {
	
	/**
	 * @return the Mode associated with this matrix 
	 */
	public Mode getMode();

	/** 
	 * @param origin the Node from which trips originate
	 * @param destination the Node to which trips travel
	 * @return the demand from the origin to the destination
	 */
	public float getDemand(TravelSurveyZone origin, TravelSurveyZone destination);
	
	/**
	 * @param origin the Node from which trips originate
	 * @param destination the Node to which trips travel
	 * @param demand the amount of trips from the origin to the destination
	 */
	public void put(TravelSurveyZone origin, TravelSurveyZone destination, Float demand);

	public TimePeriod timePeriod();
	
	public Collection<TravelSurveyZone> getZones();
	
	public DemandMap getDemandMap(TravelSurveyZone origin);
}
