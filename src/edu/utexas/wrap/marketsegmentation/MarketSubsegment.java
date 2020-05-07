package edu.utexas.wrap.marketsegmentation;

import java.util.function.ToDoubleFunction;

import edu.utexas.wrap.net.TravelSurveyZone;

public interface MarketSubsegment {

	public ToDoubleFunction<TravelSurveyZone> attributeDataGetter();
	
	public Integer depth();
	
	public String index(int axis);

}
