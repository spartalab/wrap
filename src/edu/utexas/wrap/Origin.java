package edu.utexas.wrap;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Origin extends Node{
	private Bush bush;
	private final Set<Integer> destinations;
	private final Map<Integer, Float> destDemand;
	

	public Origin(Node self, HashMap<Integer, Float> dests) {
		super(self.getIncomingLinks(), self.getOutgoingLinks(), self.getID());
		destDemand = dests;	// store demand HashMap
		destinations = dests.keySet();
	}


	public Bush getBush() {
		return bush;
	}

	Set<Integer> getDests() {

		return destinations;
	}
	
	Float getDemand(Integer n) {
		return destDemand.get(n);
	}
	
	/** Build the origin's initial bush using Dijkstra's algorithm
	 * 
	 * Create from the full network an initial bush by finding the
	 * shortest path to each node in the network from the origin, 
	 * then selecting the paths which lead to destinations to which
	 * the origin has demand.
	 * 
	 * @param links all links in the network
	 * @param nodes all nodes in the network
	 */
	public void buildBush(Set<Link> links, Map<Integer, Node> nodes) {
		bush = new Bush(this, nodes, links);
	}
}
