package edu.utexas.wrap.demand.containers;

import java.util.HashMap;

import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.net.Node;

public class AggregatePAHashMatrix extends HashMap<Node, DemandHashMap> implements AggregatePAMatrix {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4656252954122078293L;

	@Override
	public void put(Node i, DemandMap d) {
		put(i,d);
	}

	@Override
	public Object getAttribute(String type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public float getVOT() {
		// TODO Auto-generated method stub
		return 0;
	}

}
