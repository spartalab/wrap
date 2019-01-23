package edu.utexas.wrap.demand;

import java.util.HashMap;

import edu.utexas.wrap.net.Node;

public class AggregateOriginDestinationMatrix extends HashMap<Node, DemandMap> implements OriginDestinationMatrix {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4760691392227828734L;

	@Override
	public Float getDemand(Node origin, Node destination) {
		// TODO Auto-generated method stub
		return get(origin) == null ? null : get(origin).getOrDefault(destination, 0.0F);
	}

	@Override
	public void put(Node origin, Node destination, Float demand) {
		// TODO Auto-generated method stub

	}

}
