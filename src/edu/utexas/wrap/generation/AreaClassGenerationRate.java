package edu.utexas.wrap.generation;

import edu.utexas.wrap.net.TravelSurveyZone;

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
