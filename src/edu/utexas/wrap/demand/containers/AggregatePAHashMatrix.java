package edu.utexas.wrap.demand.containers;

import java.util.HashMap;

import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;

public class AggregatePAHashMatrix extends HashMap<Node, DemandHashMap> implements AggregatePAMatrix {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4656252954122078293L;
	private Graph g;

	public AggregatePAHashMatrix(Graph g) {
		this.g = g;
	}

	/**
	 * Insert the demand map for a given node
	 */
	public void putDemand(Node i, DemandHashMap d) {
		put(i, d);
	}

	/**
	 * Returns the demand given an origin and destination pair
	 */
	@Override
	public Float getDemand(Node origin, Node destination) {
		// TODO Auto-generated method stub
		return get(origin) == null ? 0.0F : get(origin).getOrDefault(destination, 0.0F);
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

	/* (non-Javadoc)
	 * @see edu.utexas.wrap.demand.PAMatrix#put(edu.utexas.wrap.net.Node, edu.utexas.wrap.net.Node, java.lang.Float)
	 */
	@Override
	public void put(Node origin, Node destination, Float demand) {
		putIfAbsent(origin,new DemandHashMap(g));
		get(origin).put(destination,demand);
		
	}

}
