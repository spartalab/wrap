package edu.utexas.wrap.demand;

import java.util.HashMap;

import edu.utexas.wrap.net.Node;

public class DemandMap extends HashMap<Node, Float>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8268461681839852205L;

	public DemandMap() {
		super();
	}
	
	protected DemandMap(DemandMap d) {
		super(d);
	}
}
