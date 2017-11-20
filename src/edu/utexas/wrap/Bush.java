package edu.utexas.wrap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class Bush {

	// Bush structure
	private final Origin origin;
	//This needs to have a topological order
	private final LinkedList<Node> bushNodes; 
	private Set<Link> activeLinks;
	private Set<Link> inactiveLinks;
	
	// Labels (for solving)
	private Map<Integer, Float> 	nodeL;
	private Map<Integer, Float>		nodeU;
	private Map<Integer, Link> 		qShort;
	private Map<Integer, Link>		qLong;
	private Map<Link, Double> 		flow;

	
	public Bush(
			Origin origin,  
			Set<Link> activeLinks,
			Set<Link> inactiveLinks) 
	{
		super();
		this.origin 		= origin;
		this.activeLinks 	= activeLinks;
		this.inactiveLinks	= inactiveLinks;
		this.bushNodes 		= getTopologicalOrder();

	}

	
	/** Calculate a topological order using Kahn's algorithm
	 * 
	 * Evaluate the set of bush links, starting from the origin
	 * and determine a topological order for the nodes that they
	 * attach
	 * @return a topological ordering of this bush's nodes
	 */
	private LinkedList<Node> getTopologicalOrder() {
		// Start with a set of all bush edges
		Set<Link> links = new HashSet<Link>(activeLinks);
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
				if (links.contains(l)) {
					
					// remove the links from the set
					links.remove(l);
					// the node on the other end
					Node m = l.getHead();
					
					// see if this node has no other incoming active links
					boolean mHasIncoming = false;
					for (Link e : m.getIncomingLinks()) {
						if (links.contains(e)) {
							mHasIncoming = true;
							break;
						}
					}
					// if not, add to the list of start nodes
					if (!mHasIncoming) S.add(m);
				}
			}
		}
		if (!links.isEmpty()) {
			//throw new Exception();
			return null;
		}
		else return to;
	}


	public void setqShort(Map<Integer, Link> qShort) {
		this.qShort = qShort;
	}


	public void setNodeL(Map<Integer, Float> nodeL) {
		this.nodeL = nodeL;
	}
	
	public void runDijkstras(Boolean longest) {
		// Initialize every nodeL to infinity except this, the origin
		// Initialize the empty map of finalized nodes, the map of 
		// eligible nodes to contain this origin only, and the 
		// back-link mapping to be empty
		Set<Integer> finalized = new HashSet<Integer>();
		Set<Integer> eligible  = new HashSet<Integer>();

		nodeL.put(origin.getID(), new Float(0.0));
		nodeU.put(origin.getID(), new Float(0.0));
		eligible.add(origin.getID());

		// While not all nodes have been reached
		while (true) {
			// Find eligible node of minimal nodeL
			Node tail = null;
			for (Integer nodeID : eligible) {
				if (!longest) {	//Calculating shortest paths

					Node node = bushNodes.get(nodeID);
					if ( tail == null || nodeL.get(node.getID()) < nodeL.get(tail.getID()) ) {
						tail = node;
					}

				} else {		//Calculating longest paths
					Node node = bushNodes.get(nodeID);
					if ( tail == null || nodeU.get(node.getID()) > nodeU.get(tail.getID()) ) {
						tail = node;
					}
				}
			}
			//DEBUG CODE BELOW
			if (tail == null) break;
			//DEBUG CODE ABOVE

			// Finalize node by adding to finalized
			finalized.add(tail.getID());
			// And remove from eligible
			eligible.remove(tail.getID());

			// If all nodes finalized, terminate
			//if (finalized.size() >= bushNodes.size()) break;

			// Update labels and backnodes for links leaving node i
			for (Link link : tail.getOutgoingLinks()) {

				Node head = link.getHead();
				//TODO: We need some sort of control structure so that the algorithm
				// only looks at the links in the active set when doing bush optimization
				if (longest) {	//Longest paths search
					// This must only be done on a bush
					if (!activeLinks.contains(link)) continue;
					// We ensure this by skipping outgoing links that are inactive

					// nodeU(j) = max( nodeU(j), nodeU(i)+c(ij) )
					Float Uj    = nodeU.get(head.getID());
					Float Uicij = nodeU.get(tail.getID())+link.getTravelTime();
					if (Uj == null || Uicij > Uj) {
						nodeU.put(head.getID(), Uicij);
						qLong.put(head.getID(), link);
					}
				} else {		//Shortest paths search
					// nodeL(j) = min( nodeL(j), nodeL(i)+c(ij) )
					Float Lj    = nodeL.get(head.getID());
					Float Licij = nodeL.get(tail.getID())+link.getTravelTime();
					if (Lj == null || Licij < Lj) {
						nodeL.put(head.getID(), Licij);
						qShort.put(head.getID(), link);
					}
				}
				if (!finalized.contains(head.getID())) eligible.add(head.getID());
			}
		}
	}


}
