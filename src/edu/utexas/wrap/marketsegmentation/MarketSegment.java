package edu.utexas.wrap.marketsegmentation;

import java.util.function.ToDoubleFunction;

import edu.utexas.wrap.net.TravelSurveyZone;

public interface MarketSegment {

	public ToDoubleFunction<TravelSurveyZone> attributeDataGetter();
	
}