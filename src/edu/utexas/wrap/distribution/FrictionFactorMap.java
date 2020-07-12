package edu.utexas.wrap.distribution;

import edu.utexas.wrap.net.TravelSurveyZone;

/**A class which returns the impedance for trips
 * between two TSZs
 * 
 * @author William
 *
 */
public interface FrictionFactorMap {

	public Float get(TravelSurveyZone i, TravelSurveyZone z, float skimCost);

}
