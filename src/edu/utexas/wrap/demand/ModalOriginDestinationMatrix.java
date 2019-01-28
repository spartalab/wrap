package edu.utexas.wrap.demand;

import java.util.HashMap;

import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Node;

public class ModalOriginDestinationMatrix extends HashMap<Node, DemandMap> implements OriginDestinationMatrix {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6104047201084019367L;
	private final Mode m;
	
	public ModalOriginDestinationMatrix(Mode mode) {
		this.m = mode;
	}
	
 	public Mode getMode() {
 		return m;
 	}

	public Float getDemand(Node origin, Node destination) {
		return get(origin) == null? 0.0F : get(origin).getOrDefault(destination,0.0F);
	}

	public void put(Node origin, Node destination, Float demand) {
		putIfAbsent(origin, new DemandMap());
		get(origin).put(destination, demand);
		
	}

}
