package edu.utexas.wrap;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Origin {

	private List<Bush> bushes;
	private Map<Integer, Link> initMap;
	private final Node self;
	
	public Origin(Node self) {
		this.self = self;
		bushes = new LinkedList<Bush>();
	}

	public List<Bush> getBushes() {
		return bushes;
	}

	Double getDemand(Integer n) {
		Double demand = 0.0;
		for(Bush bush : this.bushes) {
			demand += bush.getDemand(n);
		}
		
		return demand;
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
	 * @throws Exception 
	 */
	public void buildBush(Set<Link> links, Map<Integer, Node> nodes, Float vot, Map<Integer, Float> destDemand, VehicleClass c) {
		bushes.add(new Bush(this, nodes, links, vot, destDemand, c));
	}
	
	public void buildBush(Graph g, Float vot, Map<Integer, Float> destDemand, VehicleClass c) {
		this.buildBush(g.getLinks(), g.getNodeMap(), vot, destDemand, c);
	}
	
	public int hashCode() {
		return self.getID();
	}
	
	/**Generate an initial bush (dag) by solving Dijkstra's Shortest Paths
	 * 
	 * To be called on initialization. Overwrites nodeL and qShort.
	 */
	public void buildInitMap(Map<Integer, Node> nodes) {
		// Initialize every nodeL to infinity except this, the origin
		// Initialize the empty map of finalized nodes, the map of 
		// eligible nodes to contain this origin only, and the 
		// back-link mapping to be empty
		initMap = new HashMap<Integer, Link>(nodes.size(),1.0f);
		FibonacciHeap<Integer> Q = new FibonacciHeap<Integer>(nodes.size(),1.0f);
		for (Node n : nodes.values()) {
			if (!n.equals(self)) {
				Q.add(n.getID(), Double.MAX_VALUE);
			}
		}
		Q.add(self.getID(), 0.0);

		while (!Q.isEmpty()) {
			Leaf<Integer> u = Q.poll();
			
			
			for (Link uv : nodes.get(u.n).getOutgoingLinks()) {
//				if (!uv.allowsClass(c) || isInvalidConnector(uv)) continue;
				//If this link doesn't allow this bush's class of driver on the link, don't consider it
				
				Leaf<Integer> v = Q.getLeaf(uv.getHead().getID());
				Double alt = uv.freeFlowTime()+u.key;
				if (alt<v.key) {
					Q.decreaseKey(v, alt);
					initMap.put(v.n, uv);
				}
			}
		}
	}

	public Map<Integer, Link> getInitMap(Map<Integer, Node> nodes) {
		if (initMap == null) buildInitMap(nodes);
		return initMap;
	}
	
	public Node getNode() {
		return self;
	}
}
