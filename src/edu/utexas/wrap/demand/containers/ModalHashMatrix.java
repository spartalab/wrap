package edu.utexas.wrap.demand.containers;

import java.util.HashMap;

import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.ModalODMatrix;
import edu.utexas.wrap.demand.ModalPAMatrix;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;

public class ModalHashMatrix extends HashMap<Node, DemandMap> implements ModalODMatrix, ModalPAMatrix {
	
	
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
	
 	public Mode getMode() {
 		return m;
 	}

	public Float getDemand(Node origin, Node destination) {
		return get(origin) == null? 0.0F : get(origin).getOrDefault(destination,0.0F);
	}

	public void put(Node origin, Node destination, Float demand) {
		putIfAbsent(origin, new DemandHashMap(getGraph()));
		((DemandHashMap) get(origin)).put(destination, demand);
		
	}

	@Override
	public void putDemand(Node i, DemandMap d) {
		put(i, d);
	}

	@Override
	public Object getAttribute(String type) { return null; }

	@Override
	public float getVOT() {
		return 0;
	}

	@Override
	public Graph getGraph() {
		return g;
	}


}
