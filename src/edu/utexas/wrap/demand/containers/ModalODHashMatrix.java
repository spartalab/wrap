package edu.utexas.wrap.demand.containers;

import java.util.HashMap;

import edu.utexas.wrap.demand.ModalODMatrix;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Node;

public class ModalODHashMatrix extends HashMap<Node, DemandHashMap> implements ModalODMatrix {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6104047201084019367L;
	private final Mode m;
	
	public ModalODHashMatrix(Mode mode) {
		this.m = mode;
	}
	
 	public Mode getMode() {
 		return m;
 	}

	public Float getDemand(Node origin, Node destination) {
		return get(origin) == null? 0.0F : get(origin).getOrDefault(destination,0.0F);
	}

	public void put(Node origin, Node destination, Float demand) {
		putIfAbsent(origin, new DemandHashMap());
		get(origin).put(destination, demand);
		
	}

}
