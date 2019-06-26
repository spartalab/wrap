package edu.utexas.wrap.net;

public class TravelSurveyZone {
	private final Node origin;
	private RegionalAreaAnalysisZone parent;
	
	public TravelSurveyZone(Node origin) {
		this.origin = origin;
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
}
