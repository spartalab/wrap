package edu.utexas.wrap.demand.containers;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;

public class AggregatePAHashMatrix extends HashMap<Node, DemandMap> implements AggregatePAMatrix {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4656252954122078293L;
	private Graph g;

	public AggregatePAHashMatrix(Graph g) {
		this.g = g;
	}

	@Override
	public void putDemand(Node i, DemandMap d) {
		put(i, d);
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

	@Override
	public Graph getGraph() {
		return g;
	}

}
