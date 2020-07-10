package edu.utexas.wrap.net;

import edu.utexas.wrap.TimePeriod;

public interface NetworkSkim {

	public TimePeriod timePeriod();
	
	public float getCost(TravelSurveyZone producer, TravelSurveyZone attractor);

}
