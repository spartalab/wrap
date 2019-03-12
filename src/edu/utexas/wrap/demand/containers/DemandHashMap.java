package edu.utexas.wrap.demand.containers;

import java.util.HashMap;

import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.net.Node;

public class DemandHashMap extends HashMap<Node, Float> implements DemandMap {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8268461681839852205L;

	public DemandHashMap() {
		super();
	}
	
	protected DemandHashMap(DemandHashMap d) {
		super(d);
	}

	@Override
	public Float get(Node dest) {
		return this.getOrDefault(dest, 0.0F);
	}
}
