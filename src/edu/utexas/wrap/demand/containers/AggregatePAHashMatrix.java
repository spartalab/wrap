package edu.utexas.wrap.demand.containers;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;
import edu.utexas.wrap.modechoice.Mode;

public class AggregatePAHashMatrix extends HashMap<Node, DemandMap> implements AggregatePAMatrix {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4656252954122078293L;
	private Graph g;
	private Mode m;

	public AggregatePAHashMatrix(Graph g, Mode m) {
		this.g = g;
		this.m = m;
	}

	/**
	 * Insert the demand map for a given node
	 */
	@Override
	public void putDemand(Node i, DemandMap d) {
		put(i, d);
	}

	/**
	 * Returns the demand given an origin and destination pair
	 */
	@Override
	public Float getDemand(Node origin, Node destination) {
		// TODO Auto-generated method stub
		return get(origin) == null ? null : get(origin).getOrDefault(destination, 0.0F);
	}

	/**
	 * Takes the name of the attribute and returns a value
	 */
	@Override
	public Object getAttribute(String type) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Returns the value of time
	 */
	@Override
	public float getVOT() {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * Returns the graph associated with the hash matrix
	 */
	@Override
	public Graph getGraph() {
		return g;
	}

	/**
	 * Returns the mode associated with the matrix
	 */
	@Override
	public Mode getMode() {
		return m;
	}

}
