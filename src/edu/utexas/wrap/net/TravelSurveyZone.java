package edu.utexas.wrap.net;

public class TravelSurveyZone {
	private final int nodeID;
	private final int order;
//	private RegionalAreaAnalysisZone parent;
	private final AreaClass ac;
	
	//Demographic data

	
	
	public TravelSurveyZone(int nodeID, int order, AreaClass ac) {
		this.nodeID = nodeID;
		this.order = order;
		this.ac = ac;
	}
	
	public int getID() {
		return nodeID;
	}
	
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
