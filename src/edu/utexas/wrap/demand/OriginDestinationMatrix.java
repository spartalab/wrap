package edu.utexas.wrap.demand;

import edu.utexas.wrap.net.Node;

public interface OriginDestinationMatrix {

	public Float getDemand(Node origin, Node destination);
	
	public void put(Node origin, Node destination, Float demand);

}
