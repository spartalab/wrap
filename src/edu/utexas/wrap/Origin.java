package edu.utexas.wrap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Origin extends Node{
	private Bush bush;
	private final Set<Node> destinations;
	private final Map<Node, Double> destDemand;
	

	public Origin(Node self, HashMap<Node, Double> dests) {
		super(self.getIncomingLinks(), self.getOutgoingLinks(), self.getID());
		destDemand = dests;	// store demand HashMap
		destinations = dests.keySet();
	}


	public Bush getBush() {
		return bush;
	}

	private void setBush(Bush bush) {
		this.bush = bush;
	}

	private Set<Node> getDests() {
		return destinations;
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
		////////////////////////////////////////////////
		// PART 0: Dijkstra's algorithm (to all nodes)
		////////////////////////////////////////////////
		
		// Initialize every nodeL to infinity except this, the origin
		// Initialize the empty map of finalized nodes, the map of 
		// eligible nodes to contain this origin only, and the 
		// back-link mapping to be empty
		Map<Node, Double> tempNodeL	 = new HashMap<Node, Double>();
		Set<Node> finalized = new HashSet<Node>();
		Set<Node> eligible  = new HashSet<Node>();
		Map<Node, Link> tempBackMap  = new HashMap<Node, Link>();
		tempNodeL.put(this, new Double(0.0));
		eligible.add(this);
		
		// While not all nodes have been reached
		while (true) {
			// Find eligible node of minimal nodeL
			Node i = null;
			for (Node node : eligible) {
				if ( i == null || tempNodeL.get(node) < tempNodeL.get(i) ) {
					i = node;
				}
			}
			
			// Finalize node by adding to finalized
			finalized.add(i);
			// And remove from eligible
			eligible.remove(i);
			
			// If all nodes finalized, terminate
			if (finalized.size() >= nodes.size()) break;
			
			// Update labels and backnodes for links leaving node i
			for (Link link : i.getOutgoingLinks()) {
				Node j = link.getHead();
				
				// nodeL(j) = min( nodeL(j), nodeL(i)+c(ij) )
				Double Lj    = tempNodeL.get(j);
				Double Licij = tempNodeL.get(i)+link.getTravelTime();
				if (Lj == null || Licij < Lj) {
					tempNodeL.put(j, Licij);
					tempBackMap.put(j, link);
				}
				eligible.add(i);
			}
		}
		
		/* 
		 * We have now determined the shortest paths to all nodes in the network
		 * The next step is to reduce this down by selecting only the paths leading
		 * to the destinations for which this origin has demand.
		 */
		
		/////////////////////////////////////////////
		// Part 1: Collect shortest-path used links
		/////////////////////////////////////////////
		// Create nodeL and qShort mappings
		Map<Node, Double> nodeL = new HashMap<Node, Double>();
		Map<Node, Link> qShort = new HashMap<Node, Link>();
		Set<Node> bushNodes = new HashSet<Node>();
		Set<Link> activeLinks = new HashSet<Link>();
		Set<Link> inactiveLinks = new HashSet<Link>();
		
		// Set the origin node values
		nodeL.put(this, new Double(0.0));
		qShort.put(this, null);
		bushNodes.add(this);
		
		// For each destination
		for (Node node : getDests()) {
			// While trace hasn't encountered a stored node
			while (!bushNodes.contains(node)) {
				// Store the node and trace up the tree
				bushNodes.add(node);					// Nodes in Bush
				nodeL.put(node, tempNodeL.get(node));	// Li Labels
				Link backLink = tempBackMap.get(node);
				qShort.put(node, backLink);				// q pointers
				activeLinks.add(backLink);				// Active links
				node = backLink.getTail();				
			}
		}
		
		for (Node node : getDests()) { 
			for (Link link : node.getOutgoingLinks()) {
				if (bushNodes.contains(link.getHead()) && !activeLinks.contains(link)) {
					inactiveLinks.add(link);
				}
			}
		}
		
		/*
		 * We now have the set of active and inactive links and nodes in the bush
		 */
		Bush bush = new Bush(this, activeLinks, inactiveLinks);
		bush.setqShort(qShort);
		bush.setNodeL(nodeL);
		setBush(bush);
	}



}
