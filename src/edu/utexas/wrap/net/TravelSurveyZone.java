package edu.utexas.wrap.net;

import java.util.Map;

public class TravelSurveyZone {
	private final Node origin;
	private final int order;
	private RegionalAreaAnalysisZone parent;
	
	public TravelSurveyZone(Node origin, int order, Map<String,Float> attrs) {
		this.origin = origin;
		this.order = order;
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

	public Float valueOf(String attribute) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getOrder() {
		return order;
	}
}
