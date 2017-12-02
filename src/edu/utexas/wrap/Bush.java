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
	private Map<Integer, Float> 	nodeL;
	private Map<Integer, Float>		nodeU;
	private Map<Integer, Link> 		qShort;
	private Map<Integer, Link>		qLong;
	private Map<Link, Float> 		flow;

	
	public Bush(Origin origin, Map<Integer,Node> nodes, Set<Link> links) 
	{
		this.origin = origin;
		this.links = new HashMap<Link,Boolean>();
		for (Link l : links) this.links.put(l, false);
		this.nodes	= nodes;
		nodeL	= new HashMap<Integer, Float>();
		nodeU	= new HashMap<Integer, Float>();
		qShort	= new HashMap<Integer, Link>();
		qLong	= new HashMap<Integer, Link>();
		flow	= new HashMap<Link, Float>();
		runDijkstras(false);
		dumpFlow();
		nodeL	= new HashMap<Integer, Float>();
	}

	/**Add to the bush's flow on a link
	 * @param l the link for which flow should be added
	 * @param f the amount of flow to add to the link
	 */
	void addFlow(Link l, Float f) {
		Float x0 = flow.get(l);
		if (x0 != null) flow.put(l, x0 + f);
		else flow.put(l, f);
		links.put(l, true);
		l.addFlow(f);
	}
	
	/**Subtract from the bush's flow on a link and mark inactive if needed
	 * 
	 * @param l the link for which flow should be removed
	 * @param f the amount of flow to subtract from the link
	 */
	void subtractFlow(Link l, Float f) {
		Float newFlow = flow.get(l) - f;
		flow.put(l, newFlow); // Keep track of new value of flow from bush
		if (newFlow <= 0) {
			// Check to see if this link is needed for connectivity
			Boolean needed = true;
			for (Link i : l.getHead().getIncomingLinks()) {
				if (!i.equals(l) && links.get(i)) {
					needed = false;
					break;
				}
			}
			if (!needed) links.put(l, false);	// deactivate link in bush if no flow left
		}
		l.subtractFlow(f); // Subtract flow from the link itself (which keeps track of overall flow)
	}
	
	/**Initialize demand flow on shortest paths
	 * Add each destination's demand to the shortest path to that destination
	 * */
	private void dumpFlow() {
		for (Integer node : nodes.keySet()) {
			Float x = origin.getDemand(node);
			if (x == null) x = new Float(0);
			if (nodes.get(node).getIncomingLinks().isEmpty()) continue;
			while (!node.equals(origin.getID())) {
				
				Link back = qShort.get(node);
				addFlow(back, x);
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
	 */
	public LinkedList<Node> getTopologicalOrder() {
		// Start with a set of all bush edges
		Set<Link> currentLinks = new HashSet<Link>();
		for (Link l : links.keySet()) {
			if (links.get(l)) currentLinks.add(l);
		}
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
			//throw new Exception();
			return null;
		}
		else return to;
	}


	public void runDijkstras(Boolean longest) {
		// Initialize every nodeL to infinity except this, the origin
		// Initialize the empty map of finalized nodes, the map of 
		// eligible nodes to contain this origin only, and the 
		// back-link mapping to be empty
		Set<Integer> finalized = new HashSet<Integer>();
		Set<Integer> eligible  = new HashSet<Integer>();
		if (!longest) {
			nodeL = new HashMap<Integer, Float>();
			qShort = new HashMap<Integer, Link>();
		}
		else {
			nodeU = new HashMap<Integer, Float>();
			qLong = new HashMap<Integer, Link>();
		}
		nodeL.put(origin.getID(), new Float(0.0));
		nodeU.put(origin.getID(), new Float(0.0));
		eligible.add(origin.getID());

		// While not all nodes have been reached
		while (true) {
			// Find eligible node of minimal nodeL
			Node tail = null;
			for (Integer nodeID : eligible) {
				if (!longest) {	//Calculating shortest paths

					Node node = nodes.get(nodeID);
					if ( tail == null || nodeL.get(node.getID()) < nodeL.get(tail.getID()) ) {
						tail = node;
					}

				} else {		//Calculating longest paths
					Node node = nodes.get(nodeID);
					if ( tail == null || nodeU.get(node.getID()) > nodeU.get(tail.getID()) ) {
						tail = node;
					}
				}
			}
			if (tail == null) break;

			// Finalize node by adding to finalized
			finalized.add(tail.getID());
			// And remove from eligible
			eligible.remove(tail.getID());

			// If all nodes finalized, terminate
			//if (finalized.size() >= bushNodes.size()) break;

			// Update labels and backnodes for links leaving node i
			for (Link link : tail.getOutgoingLinks()) {

				Node head = link.getHead();
				if (longest) {	//Longest paths search
					// This must only be done on bush links
					if (!links.get(link)) continue; //So skip this link if it is inactive in the bush
					// We ensure this by skipping outgoing links that are inactive

					// nodeU(j) = max( nodeU(j), nodeU(i)+c(ij) )
					Float Uj    = nodeU.get(head.getID());
					Float Uicij = nodeU.get(tail.getID())+link.getTravelTime();
					if (Uj == null || Uicij >= Uj) {
						nodeU.put(head.getID(), Uicij);
						qLong.put(head.getID(), link);
					}
				} else {		//Shortest paths search
					// nodeL(j) = min( nodeL(j), nodeL(i)+c(ij) )
					Float Lj    = nodeL.get(head.getID());
					Float Licij = nodeL.get(tail.getID())+link.getTravelTime();
					if (Lj == null || Licij <= Lj) {
						nodeL.put(head.getID(), Licij);
						qShort.put(head.getID(), link);
					}
				}
				if (!finalized.contains(head.getID())) eligible.add(head.getID());
			}
		}
	}

	Link getqShort(Node n) {
		return qShort.get(n.getID());
	}
	
	Link getqLong(Node n) {
		return qLong.get(n.getID());
	}
	
	Float getU(Node n) {
		return nodeU.get(n.getID());
	}
	
	Float getL(Node n) {
		return nodeL.get(n.getID());
	}
	
	Float getBushFlow(Link l) {
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
