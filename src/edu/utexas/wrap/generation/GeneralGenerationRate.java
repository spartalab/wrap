package edu.utexas.wrap.generation;

import edu.utexas.wrap.net.TravelSurveyZone;

public class GeneralGenerationRate implements GenerationRate {

	private final double rate;
	
	public GeneralGenerationRate(double rate) {
		this.rate = rate;
	}

	@Override
	public double getRate(TravelSurveyZone segment) {
		return rate;
	}

	public String toString() {
		return Double.toString(rate);
	}
}
