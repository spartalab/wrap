package edu.utexas.wrap.marketsegmentation;

import java.util.function.ToDoubleFunction;

import edu.utexas.wrap.net.TravelSurveyZone;

public abstract class MarketSegment {
	private Double rate;
	
	public MarketSegment(Double segmentRate) {
		rate = segmentRate;
	}
	
	public abstract ToDoubleFunction<TravelSurveyZone> getAttributeData();
	
	public Double getRate() {
		return rate;
	}
	
}