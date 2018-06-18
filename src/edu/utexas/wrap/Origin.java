package edu.utexas.wrap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Origin extends Node{
	//private Bush bush;
	private List<Bush> bushes;
	private final Set<Integer> destinations;
	
	public Origin(Node self, Set<Integer> dests) {
		super(self.getIncomingLinks(), self.getOutgoingLinks(), self.getID());
//		destDemand = dests;	// store demand HashMap
		destinations = dests;
		bushes = new ArrayList<Bush>();
	}


	public List<Bush> getBushes() {
		return bushes;
	}

	Set<Integer> getDests() {

		return destinations;
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
	public void buildBush(Set<Link> links, Map<Integer, Node> nodes, Double vot, Map<Integer, Double> destDemand) throws Exception {
		bushes.add(new Bush(this, nodes, links, vot, destDemand));
	}
	
	public int hashCode() {
		return getID();
	}
}
