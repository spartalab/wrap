package edu.utexas.wrap;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class Bush {

	// Bush structure
	private final Origin origin;
	//This needs to have a topological order
	private Map<Integer, Node> nodes; 
	private Map<Link, Boolean> links;
	
	// Labels (for solving)
	private Map<Integer, Double> 		nodeL;
	private Map<Integer, Double>		nodeU;
	private Map<Integer, Link> 		qShort;
	private Map<Integer, Link>		qLong;
	private Map<Link, Double> 		flow;
	
	enum DijkCases {LONGEST, INITIAL, EQUILIBRATE_SHORTEST};

	
	public Bush(Origin origin, Map<Integer,Node> nodes, Set<Link> links) throws Exception 
	{
		this.origin = origin;
		this.links = new HashMap<Link,Boolean>();
		flow	= new HashMap<Link, Double>();
		for (Link l : links) {
			this.links.put(l, false);
			flow.put(l, new Double(0));
		}
		this.nodes	= nodes;
		nodeL	= new HashMap<Integer, Double>();
		nodeU	= new HashMap<Integer, Double>();
		qShort	= new HashMap<Integer, Link>();
		qLong	= new HashMap<Integer, Link>();
		
		runDijkstras(DijkCases.INITIAL);
		dumpFlow();
		//nodeL	= new HashMap<Integer, Double>();
	}

	/**Add to the bush's flow on a link
	 * @param l the link for which flow should be added
	 * @param f the amount of flow to add to the link
	 */
	void addFlow(Link l, Double f) {
		Double x0 = flow.get(l);
		if (x0 != null) flow.put(l, x0 + f);
		else flow.put(l, f);
	}
	
	/**Subtract from the bush's flow on a link and mark inactive if needed
	 * 
	 * @param l the link for which flow should be removed
	 * @param f the amount of flow to subtract from the link
	 */
	void subtractFlow(Link l, Double f) {
		Double newFlow = flow.get(l) - f;
		flow.put(l, newFlow); // Keep track of new value of flow from bush

		
	}
	
	/**Initialize demand flow on shortest paths
	 * Add each destination's demand to the shortest path to that destination
	 * */
	private void dumpFlow() {
		for (Integer node : nodes.keySet()) {
			Double x = origin.getDemand(node);
			if (x == null) x = new Double(0);
			if (nodes.get(node).getIncomingLinks().isEmpty()) continue;
			while (!node.equals(origin.getID())) {
				
				Link back = qShort.get(node);
				addFlow(back, x);
				links.put(back, true);
				back.addFlow(x);
				node = back.getTail().getID();
			} 
		}

	}
	
	/** Calculate a topological order using Kahn's algorithm
	 * 
	 * Evaluate the set of bush links, starting from the origin
	 * and determine a topological order for the nodes that they
	 * attach
	 * @return a topological ordering of this bush's nodes
	 * @throws Exception 
	 */
	public LinkedList<Node> getTopologicalOrder() throws Exception {
		// Start with a set of all bush edges
		Set<Link> currentLinks = new HashSet<Link>();
		for (Link l : links.keySet()) if (links.get(l)) currentLinks.add(l);
		
		LinkedList<Node> to = new LinkedList<Node>();
		LinkedList<Node> S = new LinkedList<Node>();
		// "start nodes"
		S.add(origin);
		Node n;

		while (!S.isEmpty()) {
			n = S.pop();// remove node from S
			to.add(n); 	// append node to L
			
			// for each active edge out of this node
			for (Link l : n.getOutgoingLinks()) {
				if (currentLinks.contains(l)) {
					
					// remove the links from the set
					currentLinks.remove(l);
					// the node on the other end
					Node m = l.getHead();
					
					// see if this node has no other incoming active links
					boolean mHasIncoming = false;
					for (Link e : m.getIncomingLinks()) {
						if (currentLinks.contains(e)) {
							mHasIncoming = true;
							break;
						}
					}
					// if not, add to the list of start nodes
					if (!mHasIncoming) S.add(m);
				}
			}
		}
		if (!currentLinks.isEmpty()) {
			throw new Exception();
			//return null;
		}
		else return to;
	}


	public void runDijkstras(DijkCases type) throws Exception {
		// Initialize every nodeL to infinity except this, the origin
		// Initialize the empty map of finalized nodes, the map of 
		// eligible nodes to contain this origin only, and the 
		// back-link mapping to be empty
		Set<Integer> eligible  = new HashSet<Integer>();
		
		nodeL = new HashMap<Integer, Double>();
		qShort = new HashMap<Integer, Link>();
		for (Integer l : nodes.keySet()) {
			nodeL.put(l, Double.POSITIVE_INFINITY);
			eligible.add(l);
		}
		nodeL.put(origin.getID(), new Double(0.0));
		
		// While not all nodes have been reached
		while (!eligible.isEmpty()) {
			// Find eligible node of minimal nodeL
			Node tail = null;
			for (Integer nodeID : eligible) {
				Node node = nodes.get(nodeID);
				//Calculating shortest paths
					if ( tail == null || nodeL.get(node.getID()) < nodeL.get(tail.getID()) ) 
						tail = node;

			}
//			if (tail == null) break;
			
			// Finalize node by adding to finalized
			// And remove from eligible
			eligible.remove(tail.getID());

			// Update labels and backnodes for links leaving node i
			for (Link link : tail.getOutgoingLinks()) {
				// This must only be done on bush links
				if (type != DijkCases.INITIAL && !links.get(link)) continue; //So skip this link if it is inactive in the bush
				Node head = link.getHead();

				//Shortest paths search
				// nodeL(j) = min( nodeL(j), nodeL(i)+c(ij) )
				Double Lj    = nodeL.get(head.getID());
				Double Licij = nodeL.get(tail.getID())+link.getTravelTime();
				if (Licij < Lj) {
					nodeL.put(head.getID(), Licij);
					qShort.put(head.getID(), link);
				}
				
			}
		}
	}
	//Getting nodeU and qLong (longest paths) using Depth First Search algorithm
	public void topoSearch(DijkCases type, LinkedList<Node> to) throws Exception {
		// Initialize all nodeU values as 0 and all nodes as not visited

		//SHORTEST PATHS
		if(type == DijkCases.EQUILIBRATE_SHORTEST) {
			qShort = new HashMap<Integer, Link>();
			nodeL = new HashMap<Integer, Double>();
			for (Integer i : new HashSet<Integer>(nodes.keySet())) {
				nodeL.put(i, Double.POSITIVE_INFINITY);
			}
			nodeL.put(origin.getID(), 0.0);
			
			for (Node d : to) {
				if (nodeL.get(d.getID()) == Double.POSITIVE_INFINITY)
					continue;
	
				for (Link l : d.getOutgoingLinks()) {
					if (links.get(l)) {
						Double Licij = l.getTravelTime() + nodeL.get(d.getID());
	
						if (Licij < nodeL.get(l.getHead().getID())) {
							nodeL.put(l.getHead().getID(), Licij);
							qShort.put(l.getHead().getID(), l);
						}
					}
				}
			}
		}
		
		//LONGEST PATHS
		else if(type == DijkCases.LONGEST) {
			qLong = new HashMap<Integer, Link>();
			nodeU = new HashMap<Integer, Double>();
			for (Integer i : new HashSet<Integer>(nodes.keySet())) {
				nodeU.put(i, Double.NEGATIVE_INFINITY);
			}
			nodeU.put(origin.getID(), 0.0);
			
			for (Node d : to) {
				if (nodeU.get(d.getID()) == Double.NEGATIVE_INFINITY)
					continue;

				for (Link l : d.getOutgoingLinks()) {
					if (links.get(l)) {
						Double Uicij = l.getTravelTime() + nodeU.get(d.getID());

						if (Uicij > nodeU.get(l.getHead().getID())) {
							nodeU.put(l.getHead().getID(), Uicij);
							qLong.put(l.getHead().getID(), l);
						}
					}
				}
			}
		}
		
		
//		for (Integer i : new HashSet<Integer>(nodes.keySet())) {
//			if(type == DijkCases.EQUILIBRATE_SHORTEST) {
//				nodeL.put(i, Double.POSITIVE_INFINITY);
//			}
//			else if(type == DijkCases.LONGEST) {
//				nodeU.put(i, Double.NEGATIVE_INFINITY);
//			}
//			// visited.put(i, false);
//		}
//		if(type == DijkCases.EQUILIBRATE_SHORTEST) 		nodeL.put(origin.getID(), 0.0f);
//		if(type == DijkCases.LONGEST) 		nodeU.put(origin.getID(), 0.0f);
//
//
//		for (Node d : to) {
//
//			if(type == DijkCases.EQUILIBRATE_SHORTEST) {
//				if (nodeL.get(d.getID()) == Double.POSITIVE_INFINITY)
//					continue;
//	
//				for (Link l : d.getOutgoingLinks()) {
//					if (links.get(l)) {
//						Double Licij = l.getTravelTime() + nodeL.get(d.getID());
//	
//						if (Licij < nodeL.get(l.getHead().getID())) {
//							nodeL.put(l.getHead().getID(), Licij);
//							qShort.put(l.getHead().getID(), l);
//						}
//					}
//				}
//			}
//			
//			else if(type == DijkCases.LONGEST) {
//				if (nodeU.get(d.getID()) == Double.NEGATIVE_INFINITY)
//					continue;
//
//				for (Link l : d.getOutgoingLinks()) {
//					if (links.get(l)) {
//						Double Uicij = l.getTravelTime() + nodeU.get(d.getID());
//
//						if (Uicij > nodeU.get(l.getHead().getID())) {
//							nodeU.put(l.getHead().getID(), Uicij);
//							qLong.put(l.getHead().getID(), l);
//						}
//					}
//				}
//			}
//		}

	}
 
	Link getqShort(Node n) {
		return qShort.get(n.getID());
	}
	
	Link getqLong(Node n) {
		return qLong.get(n.getID());
	}
	
	Double getU(Node n) throws Exception {
		if(nodeU.get(n.getID()) < 0) throw new Exception();
		return nodeU.get(n.getID());
	}
	
	Double getL(Node n) throws Exception {
		if(nodeL.get(n.getID()) < 0) throw new Exception();
		return nodeL.get(n.getID());
	}
	
	Double getBushFlow(Link l) throws Exception{
		if(flow.get(l) < 0) throw new Exception();
		return flow.get(l);
	}

	public Node getOrigin() {
		return origin;
	}
	
	public Map<Link, Boolean> getLinks(){
		return links;
	}

	public Collection<Node> getNodes() {
		return nodes.values();
	}
	
}
