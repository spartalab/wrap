package edu.utexas.wrap.net;

public class TravelSurveyZone {
	private final Node origin;
	private final int order;
//	private RegionalAreaAnalysisZone parent;
	private final AreaClass ac;
	
	//Demographic data

	
	
	public TravelSurveyZone(Node origin, int order, AreaClass ac) {
		this.origin = origin;
		this.order = order;
		this.ac = ac;
	}
	
	public Node node() {
		return origin;
	}
	
	public int getID() {
		return origin.getID();
	}
	
	@Override
	public String toString() {
		return "Zone "+this.getID();
	}
	
//	public void setRAA(RegionalAreaAnalysisZone parent) {
//		this.parent = parent;
//	}
//	
//	public RegionalAreaAnalysisZone getRAA() {
//		return parent;
//	}

	public int getOrder() {
		return order;
	}
	
	public AreaClass getAreaClass() {
		return ac;
	}
	
}
