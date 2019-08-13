package edu.utexas.wrap.net;

import java.util.Set;

public class RegionalAreaAnalysisZone {
	private final Set<TravelSurveyZone> tszs;
	private AreaClass type;
	
	public RegionalAreaAnalysisZone(Set<TravelSurveyZone> zones) {
		tszs = zones;
	}
	
	public Set<TravelSurveyZone> getTSZs(){
		return tszs;
	}
	
	public AreaClass getAreaType() {
		return type;
	}
}
