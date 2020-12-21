package edu.utexas.wrap.generation;

import edu.utexas.wrap.net.TravelSurveyZone;

/**This interface defines a rate at which trips
 * are generated for a given TravelSurveyZone.
 * No specifications are given on how the rate
 * is determined, and all zones may have the same
 * rate
 * 
 * @author William
 *
 */
public interface GenerationRate {

	public double getRate(TravelSurveyZone segment);
}
