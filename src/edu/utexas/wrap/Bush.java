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
	private final Double vot;
	private Map<Integer, Node> nodes; 
	private Map<Link, Boolean> links; // Map from Link to its status (active/inactive)
	
	// Labels (for solving)
	private Map<Integer, Double> 	nodeL;
	private Map<Integer, Double>	nodeU;
	private Map<Integer, Link> 		qShort;
	private Map<Integer, Link>		qLong;
	private Map<Link, Double> 		flow;
	private final Map<Integer, Double>	destDemand;
	
	
	public Bush(Origin o, Map<Integer,Node> nodes, Set<Link> links, Double vot, Map<Integer, Double> destDemand) throws Exception 
	{
		origin = o;
		this.vot = vot;
		this.destDemand = destDemand;
		//Initialize flow and status maps
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
		
		runDijkstras();
		dumpFlow();
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
				//.out.println(back.toString()+" dump: "+Double.toString(x));
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
		}
		else return to;
	}


	/**Generate an initial bush (dag) by solving Dijkstra's Shortest Paths
	 * 
	 * To be called on initialization. Overwrites nodeL and qShort.
	 * @throws Exception if link travel times are unavailable
	 */
	public void runDijkstras() throws Exception {
		// Initialize every nodeL to infinity except this, the origin
		// Initialize the empty map of finalized nodes, the map of 
		// eligible nodes to contain this origin only, and the 
		// back-link mapping to be empty
		Map<Integer, Link> back = new HashMap<Integer, Link>();
		FibonacciHeap<Integer> Q = new FibonacciHeap<Integer>();
		nodeL = new HashMap<Integer, Double>();
		for (Node n : nodes.values()) {
			if (!n.equals(origin)) {
				Q.add(n.getID(), Double.MAX_VALUE);
			}
		}
		Q.add(origin.getID(), 0.0);
		
		while (!Q.isEmpty()) {
			Leaf<Integer> u = Q.poll();
			nodeL.put(u.n, u.key);
			for (Link uv : nodes.get(u.n).getOutgoingLinks()) {
				Leaf<Integer> v = Q.getLeaf(uv.getHead().getID());
				Double alt = uv.getPrice(vot) + u.key;
				if (alt < v.key) {
					Q.decreaseKey(v, alt);
					back.put(v.n, uv);
				}
			}
		}
		qShort = back;
	}
	

	/**Calculate shortest or longest paths in bush (DAG) using topological search
	 * 
	 * Leverage the presence of a topological order to decrease search time for 
	 * shortest/longest paths calculation
	 * 
	 * @param longest switch for Longest/Shortest
	 * @param to a topological ordering of the nodes
	 * @throws Exception if the link performance functions are unavailable
	 */
	public void topoSearch(Boolean longest, LinkedList<Node> to) throws Exception {
		// Initialize all nodeU values as 0 and all nodes as not visited

		//SHORTEST PATHS
		if(!longest) {
			//Initialize infinity-filled nodeL and empty qShort
			qShort = new HashMap<Integer, Link>();
			nodeL = new HashMap<Integer, Double>();
			for (Integer i : new HashSet<Integer>(nodes.keySet())) {
				nodeL.put(i, Double.POSITIVE_INFINITY);
			}
			nodeL.put(origin.getID(), 0.0);
			
			for (Node d : to) {
				// Should only occur if there's a node with no incoming links
				if (nodeL.get(d.getID()) == Double.POSITIVE_INFINITY)
					continue;
	
				for (Link l : d.getOutgoingLinks()) {
					if (links.get(l)) {
						Double Licij = l.getPrice(vot) + nodeL.get(d.getID());
						
						Integer id = l.getHead().getID();
						if (Licij < nodeL.get(id)) {
							nodeL.put(id, Licij);
							qShort.put(id, l);
						}
					}
				}
			}
		}
		
		//LONGEST PATHS
		else  {
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
						Double Uicij = l.getPrice(vot) + nodeU.get(d.getID());
						Integer id = l.getHead().getID();
						if (Uicij > nodeU.get(id)) {
							nodeU.put(id, Uicij);
							qLong.put(id, l);
						}
					}
				}
			}
		}

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
		return flow.getOrDefault(l, 0.0);
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

	public Double getVOT() {
		// TODO Auto-generated method stub
		return vot;
	}
	
	Double getDemand(Integer n) {
		return destDemand.getOrDefault(n, 0.0);
	}
	
}
