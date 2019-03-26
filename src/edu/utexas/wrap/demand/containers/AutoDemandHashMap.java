package edu.utexas.wrap.demand.containers;

import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Graph;

/**An extension of demand maps 
 * @author William
 *
 */
public class AutoDemandHashMap extends DemandHashMap {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2176027918628424731L;
	private final AutoODHashMatrix parent;
	
	public AutoDemandHashMap(Graph g, AutoODHashMatrix parent) {
		super(g);
		this.parent = parent;
	}

	public AutoDemandHashMap(Graph g, DemandMap sub, AutoODHashMatrix parent) {
		super(g, sub);
		this.parent = parent;
	}

	/**
	 * Returns the mode
	 */
	public Mode getMode() {
		// TODO Auto-generated method stub
		return parent.getMode();
	}

	/**
	 * Returns the value of time
	 */
	public Float getVOT() {
		// TODO Auto-generated method stub
		return parent.getVOT();
	}

}
