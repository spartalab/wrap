package edu.utexas.wrap.net;

import java.util.Map;

public class TravelSurveyZone {
	private final Node origin;
	private RegionalAreaAnalysisZone parent;
	private Map<String,Float> attributes;
	
	public TravelSurveyZone(Node origin, Map<String,Float> attrs) {
		this.origin = origin;
		attributes = attrs;
	}
	
	public Node getNode() {
		return origin;
	}
	
	public void setRAA(RegionalAreaAnalysisZone parent) {
		this.parent = parent;
	}
	
	public RegionalAreaAnalysisZone getRAA() {
		return parent;
	}

	public float valueOf(String attribute) {
		// TODO Auto-generated method stub
		return attributes.getOrDefault(attribute, 0.0f);
	}
}
