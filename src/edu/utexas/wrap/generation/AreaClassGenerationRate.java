package edu.utexas.wrap.generation;

import edu.utexas.wrap.net.TravelSurveyZone;

/**A GenerationRate where each TravelSurveyZone's rate is
 * determined by its AreaClass. For example, if a TravelSurveyZone
 * is assigned the AreaClass RURAL, then the rate used for this
 * zone is the rate which was assigned to RURAL zones in general.
 * No other characteristic determines the rate associated with
 * a given zone.
 * 
 * @author William
 *
 */
public class AreaClassGenerationRate implements GenerationRate {
	private double[] rate;

	public AreaClassGenerationRate(double[] rate) {
		this.rate = rate;
	}
	
	@Override
	public double getRate(TravelSurveyZone zone) {
		return rate[zone.getAreaClass().ordinal()];
	}
	
}
