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
	
	/**Create a TSZ with defined ID number, vectorization index, and AreaClass
	 * 
	 * @param nodeID the ID associated with this TSZ and its corresponding Node in graphs
	 * @param order the index of this TSZ in a vectorized listing of all the network's TSZs
	 * @param ac the AreaClass associated with this TSZ
	 */
	public TravelSurveyZone(int nodeID, int order, AreaClass ac) {
		this.nodeID = nodeID;
		this.order = order;
		this.ac = ac;
	}
	
	/**
	 * @return the ID associated with this TSZ and its corresponding Node in graphs
	 */
	public int getID() {
		return nodeID;
	}
	
	public String toString() {
		return "Zone "+this.getID();
	}
	
	/**Attach an RAA to the zone for aggregation purposes
	 * 
	 * This method defines the singular RAA associated with this TSZ
	 * 
	 * @param parent the RAA which contains this TSZ
	 */
	public void setRAA(RegionalAreaAnalysisZone parent) {
		this.parent = parent;
	}
	
	/**
	 * @return the RAA which encapsulates this TSZ
	 */
	public RegionalAreaAnalysisZone getRAA() {
		return parent;
	}

	/**
	 * @return the index of this TSZ in a vectorized representation of the network's TSZs
	 */
	public int getOrder() {
		return order;
	}
	
	/**
	 * @return the AreaClass assigned to this TSZ
	 */
	public AreaClass getAreaClass() {
		return ac;
	}
	
}
