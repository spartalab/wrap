package edu.utexas.wrap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Origin extends Node{
	private Bush bush;
	private Set<Node> destinations;
	private Map<Node, Double> destDemand;
	private Map<Link, Double> linkFlows;
	private Map<Node, Double> nodeL;
	private Map<Node, Double> nodeU;
	private Map<Node, Link> qShort;
	private Map<Node, Link> qLong;
	

//	public Origin(List<Link> incomingLinks, List<Link> outgoingLinks, Bush bush, int[] demandVector) {
//		super(incomingLinks, outgoingLinks);
//		this.bush = bush;
//		this.demandVector = demandVector;
//	}

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
	public void buildBush(Set<Link> links, Map<Integer, Node> nodes) {
		////////////////////////////////////////////////
		// PART 0: Dijkstra's algorithm (to all nodes)
		////////////////////////////////////////////////
		
		// Initialize every nodeL to infinity except this, the origin
		// Initialize the empty map of finalized nodes, the map of 
		// eligible nodes to contain this origin only, and the 
		// back-link mapping to be empty
		Map<Node, Double> tempNodeL	 = new HashMap<Node, Double>();
		Map<Node, Boolean> finalized = new HashMap<Node, Boolean>();
		Map<Node, Boolean> eligible  = new HashMap<Node, Boolean>();
		Map<Node, Link> tempBackMap  = new HashMap<Node, Link>();
		tempNodeL.put(this, new Double(0.0));
		eligible.put(this,true);
		
		// While not all nodes have been reached
		while (true) {
			// Find eligible node of minimal nodeL
			Node i = null;
			for (Node node : eligible.keySet()) {
				if ( i == null || tempNodeL.get(node) < tempNodeL.get(i) ) {
					i = node;
				}
			}
			
			// Finalize node by adding to finalized
			finalized.put(i, true);
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
		nodeL = new HashMap<Node, Double>();
		qShort = new HashMap<Node, Link>();
		// Set the origin node values
		nodeL.put(this, new Double(0.0));
		qShort.put(this, null);
		
		// For each destination
		for (Node node : getDests()) {
			// While trace hasn't encountered a stored node
			while (!nodeL.containsKey(node)) {
				// Store the node and trace up the tree
				Link backLink = tempBackMap.get(node);
				nodeL.put(node, tempNodeL.get(node));
				qShort.put(node, backLink);
				node = backLink.getTail();
			}
		}
		/*
		 * We now have the set of links and nodes in the bush
		 */
		
		// TODO: So how do we want to store this in the bush???
	}



}
