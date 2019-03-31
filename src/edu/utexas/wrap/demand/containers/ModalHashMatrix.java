package edu.utexas.wrap.demand.containers;

import java.util.HashMap;

import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.ModalPAMatrix;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;

public class ModalHashMatrix extends HashMap<Node, DemandMap> implements ODMatrix, ModalPAMatrix {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6104047201084019367L;
	private final Mode m;
	private Graph g;

	public ModalHashMatrix(Graph g, Mode mode) {
		this.g = g;
		this.m = mode;
	}

	/**
	 * Returns the mode of the matrix
	 */
 	public Mode getMode() {
 		return m;
 	}

	/**
	 * Returns the demand given an origin and destination pair
	 */
	public Float getDemand(Node origin, Node destination) {
		return get(origin) == null? 0.0F : get(origin).getOrDefault(destination,0.0F);
	}

	/**
	 * Insert the demand for an origin/desination pair. Will replace existing value
	 */
	@Override
	public void put(Node origin, Node destination, Float demand) {
		putIfAbsent(origin, new DemandHashMap(getGraph()));
		((DemandHashMap) get(origin)).put(destination, demand);
		
	}

	/**
	 * Insert the demand for a node
	 */
	@Override
	public void putDemand(Node i, DemandMap d) {
		put(i, d);
	}

	/**
	 * Returns the attribute specified
	 */
	@Override
	public Object getAttribute(String type) { return null; }

	/**
	 * Returns the value of time
	 */
	@Override
	public float getVOT() {
		return 0;
	}

	/**
	 * Returns the graph associated with the hash matrix
	 */
	@Override
	public Graph getGraph() {
		return g;
	}


}
