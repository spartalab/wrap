package edu.utexas.wrap.distribution;

import edu.utexas.wrap.net.Node;
import edu.utexas.wrap.net.TravelSurveyZone;

public interface FrictionFactorMap {

	public Float get(TravelSurveyZone i, TravelSurveyZone z);

}
