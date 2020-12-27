package edu.utexas.wrap.net;

/**An area of land from and to which trips occur
 * 
 * A TravelSurveyZone represents an area which has
 * had its demographics and travel behavior modeled.
 * Each TSZ is associated with a centroid Node,
 * an AreaClass, and may or may not be associated
 * with a parent RegionalAreaAnalysisZone.
 * 
 * @author William
 *
 */
public class TravelSurveyZone {
	private final int nodeID;
	private final int order;
	private RegionalAreaAnalysisZone parent;
	private final AreaClass ac;
	
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
	
	public void setRAA(RegionalAreaAnalysisZone parent) {
		this.parent = parent;
	}
	
	public RegionalAreaAnalysisZone getRAA() {
		return parent;
	}

	public int getOrder() {
		return order;
	}
	
	public AreaClass getAreaClass() {
		return ac;
	}
	
}
