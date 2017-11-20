package edu.utexas.wrap;

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
	private Map<Node, Double> 	nodeL;
	private Map<Node, Double>	nodeU;
	private Map<Node, Link> 	qShort;
	private Map<Node, Link>		qLong;
	private Map<Link, Double> 	flow;

	
	public Bush(Origin origin, Set<Node> nodes, Set<Link> links) 
	{
		this.origin = origin;
		this.activeLinks = links;
		this.inactiveLinks = null;
		this.nodeL = null;
		this.nodeU = null;
		this.qShort = null;
		this.qLong = null;
		//TODO Insert Dijkstra's here
		//TODO Insert trim method here to return new activeLinks and inactiveLinks sets
		this.bushNodes = getTopologicalOrder();
		

	}

	
	private void trim() {
		
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


	public void setqShort(Map<Node, Link> qShort) {
		this.qShort = qShort;
	}


	public void setNodeL(Map<Node, Double> nodeL) {
		this.nodeL = nodeL;
	}
	
	
	
}
