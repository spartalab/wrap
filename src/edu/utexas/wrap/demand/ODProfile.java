package edu.utexas.wrap.demand;

import edu.utexas.wrap.TimePeriod;
import edu.utexas.wrap.modechoice.Mode;

/**An interface which contains travel demand over multiple
 * TimePeriods. The TimePeriods for which demand exists will
 * be associated with an ODMatrix of trips made during the
 * TimePeriod. The ODProfile also defines the mode used for
 * all trips, as well as a value of time for each TimePeriod.
 * Note that while the Mode is constant for a given ODProfile,
 * the value of time may (and most likely should) be dependent
 * on the TimePeriod
 * 
 * @author William
 *
 */
public interface ODProfile {

	/**
	 * @param period the TimePeriod whose ODMatrix should be returned
	 * @return an ODMatrix defining trips made during the given TimePeriod
	 */
	public ODMatrix getMatrix(TimePeriod period);
	
	/**
	 * @return the Mode used for trips in this ODProfile
	 */
	public Mode getMode();

	/**
	 * @param timePeriod the TimePeriod whose value of time should be returned
	 * @return a Float defining how much a unit of travel time is valued by
	 * those making trips in this ODProfile during the given TimePeriod
	 */
	public Float getVOT(TimePeriod timePeriod);
	
}