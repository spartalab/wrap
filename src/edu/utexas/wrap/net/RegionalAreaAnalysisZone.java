package edu.utexas.wrap.net;

import java.util.Set;

public class RegionalAreaAnalysisZone {
	private final Set<TravelSurveyZone> tszs;
	
	public RegionalAreaAnalysisZone(Set<TravelSurveyZone> zones) {
		tszs = zones;
	}
	
	public Set<TravelSurveyZone> getTSZs(){
		return tszs;
	}
}
