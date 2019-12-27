package edu.utexas.wrap.demand.containers;

import edu.utexas.wrap.demand.AutoDemandMap;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Graph;

/**An extension of demand maps 
 * @author William
 *
 */
public class AutoDemandHashMap extends DemandHashMap implements AutoDemandMap {

	private final AutoODHashMatrix parent;
	
	public AutoDemandHashMap(Graph g, AutoODHashMatrix parent) {
		super(g);
		this.parent = parent;
	}

	public AutoDemandHashMap(DemandHashMap sub, AutoODHashMatrix parent) {
		super(sub);
		this.parent = parent;
	}

	/**
	 * @return the Mode associated with the DemandMap
	 */
	public Mode getMode() {
		return parent.getMode();
	}

	/**
	 * @return the value of time of trips stored in the Map
	 */ 
	public Float getVOT() {
		return parent.getVOT();
	}

}
