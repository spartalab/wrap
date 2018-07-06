package edu.utexas.wrap;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Bush {

	// Bush structure
	private final Origin origin;
	private final Double vot;
	private Map<Integer, Node> nodes; 
	private Set<Link> activeLinks; // Set of active links
	
	// Labels (for solving)
	private Map<Integer, Link> 		qShort;
	private Map<Integer, Link>		qLong;
	private Map<Link, Double> 		flow;
	private LinkedList<Node>				topoOrder;
	private final Map<Integer, Double>	destDemand;
	
	
	public Bush(Origin o, Map<Integer,Node> nodes, Set<Link> links, Double vot, Map<Integer, Double> destDemand) throws Exception 
	{
		origin = o;
		this.vot = vot;
		this.destDemand = destDemand;
		//Initialize flow and status maps
		activeLinks = new HashSet<Link>(links.size(),1.0f);
		flow	= new HashMap<Link, Double>(links.size(),1.0f);
		this.nodes	= nodes;
		qShort	= new HashMap<Integer, Link>(nodes.size(),1.0f);
		qLong	= new HashMap<Integer, Link>(nodes.size(),1.0f);
		
		runDijkstras();
		dumpFlow();
	}

	/**Add to the bush's flow on a link
	 * @param l the link for which flow should be added
	 * @param f the amount of flow to add to the link
	 */
//	void addFlow(Link l, Double f) {
//		l.addFlow(f);
////		if (f < 0.0) throw new RuntimeException("flow is "+f.toString());
//		Double x0 = flow.getOrDefault(l,0.0);
//		Double x1 = x0 + f;
//		flow.put(l, Double.max(x1,0.0));
//		if (x1<0.0) {
//			System.err.println(x1);
//		}
//	}
	
	void changeFlow(Link l, Double delta) {
		if (l.getFlow() + delta < 0.0 )	{	
			throw new NegativeFlowException("Removed too much link flow");
		
		}
		if (getBushFlow(l) + delta < 0.0) 	throw new NegativeFlowException("Removed too much bush flow");
		l.changeFlow(delta);
		Double d = getBushFlow(l)+delta;
		if (d > 0.0) {
			flow.put(l, d);
			if (flow.get(l) > l.getFlow()) {
				throw new NegativeFlowException("bush flow higher than link flow");
			}
		}
		else flow.remove(l);
		
	}
	
	/**Subtract from the bush's flow on a link and mark inactive if needed
	 * 
	 * @param l the link for which flow should be removed
	 * @param f the amount of flow to subtract from the link
	 */
//	void subtractFlow(Link l, Double f) {
//		l.subtractFlow(f);
//		Double newFlow = flow.getOrDefault(l,0.0) - f;
//		flow.put(l, Double.max(newFlow,0.0)); // Keep track of new value of flow from bush
//		if (newFlow<0.0) System.err.println(newFlow);
//		else if (newFlow == 0.0) {
//			flow.remove(l);
//		}
//	}
	
	/**Initialize demand flow on shortest paths
	 * Add each destination's demand to the shortest path to that destination
	 * */
	private void dumpFlow() {
		for (Integer node : nodes.keySet()) {
			Double x = getDemand(node);
			if (x == null) x = 0.0;
			
			if (nodes.get(node).getIncomingLinks().isEmpty()) continue;
			while (!node.equals(origin.getID())) {
				Link back = qShort.get(node);
				//.out.println(back.toString()+" dump: "+Double.toString(x));
				changeFlow(back, x);
				markActive(back);
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
		return (topoOrder != null) ?  topoOrder :  generateTopoOrder();
	}

	/**
	 * @return
	 * @throws Exception
	 */
	private LinkedList<Node> generateTopoOrder() throws Exception {
		// Start with a set of all bush edges
		Set<Link> currentLinks = new HashSet<Link>();
		for (Link l : activeLinks) if (isActive(l)) currentLinks.add(l);
		
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
		Map<Integer, Link> back = new HashMap<Integer, Link>(nodes.size(),1.0f);
		FibonacciHeap<Integer> Q = new FibonacciHeap<Integer>(nodes.size(),1.0f);
		for (Node n : nodes.values()) {
			if (!n.equals(origin)) {
				Q.add(n.getID(), Double.MAX_VALUE);
			}
		}
		Q.add(origin.getID(), 0.0);
		
		while (!Q.isEmpty()) {
			Leaf<Integer> u = Q.poll();
//			nodeL.put(u.n, u.key);
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
	public void topoSearch(Boolean longest) throws Exception {
		// Initialize all nodeU values as 0 and all nodes as not visited
		List<Node> to = getTopologicalOrder();
		//SHORTEST PATHS
		if(!longest) {
			//Initialize infinity-filled nodeL and empty qShort
			qShort = new HashMap<Integer, Link>(nodes.size(),1.0f);
			for (Node d : to) {
				// Should only occur if there's a node with no incoming links
//				if (nodeL.get(d.getID()) == Double.POSITIVE_INFINITY)
//					continue;
	
				for (Link l : d.getOutgoingLinks()) {
					if (isActive(l)) {
						Double Licij = l.getPrice(vot) + getL(d);
						
						Node head = l.getHead();
						Integer id = l.getHead().getID();
						if (qShort.get(id) == null || Licij < getL(head)) {
							qShort.put(id, l);
						}
					}
				}
			}
		}
		
		//LONGEST PATHS
		else  {
			qLong = new HashMap<Integer, Link>(nodes.size(),1.0f);
			for (Node d : to) {
				for (Link l : d.getOutgoingLinks()) {
					if (isActive(l)) {
						Double Uicij = l.getPrice(vot) + getU(d);
						Node head = l.getHead();
						Integer id = l.getHead().getID();
						if (qLong.get(id) == null || Uicij > getU(head)) {
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
	
	Path getShortestPath(Node n) {
		Path p = new Path();
		Link curLink = getqShort(n);
		while (curLink != null) {
			p.addFirst(curLink);
			curLink = getqShort(curLink.getTail());
		}
		return p;
	}
	
	Path getLongestPath(Node n) {
		Path p = new Path();
		Link curLink = getqLong(n);
		while (curLink != null) {
			p.addFirst(curLink);
			curLink = getqLong(curLink.getTail());
		}
		return p;
	}
	
	Double getU(Node n) throws Exception {

		Link back = qLong.get(n.getID());
		if (n.equals(origin)) return 0.0;
		else if (back == null) throw new UnreachableException(n,this);
		else return getU(back.getTail()) + back.getPrice(vot);

	}
	
	Double getL(Node n) throws Exception {

		Link back = qShort.get(n.getID());
		if (n.equals(origin)) return 0.0;
		else if (back == null) throw new UnreachableException(n,this);
		else return getL(back.getTail()) + back.getPrice(vot);
	}
	
	Double getBushFlow(Link l) {
		if (flow.getOrDefault(l,0.0) < 0.0) throw new NegativeFlowException("Negative bush flow");
		return flow.getOrDefault(l, 0.0);
	}

	public Node getOrigin() {
		return origin;
	}
	
	public Set<Link> getLinks(){
		return activeLinks;
	}

	public Collection<Node> getNodes() {
		return nodes.values();
	}

	public Double getVOT() {
		return vot;
	}
	
	Double getDemand(Integer n) {
		return destDemand.getOrDefault(n, 0.0);
	}
	
	public String toString() {
		return "Bush "+origin.getID()+"-VOT="+vot;
	}
	
	public void markActive(Link l) {
		if (activeLinks.add(l)) topoOrder = null;
	}
	
	public void markInactive(Link l) {
		if (activeLinks.remove(l)) topoOrder = null;
	}
	
	public boolean isActive(Link l) {
		return activeLinks.contains(l);
	}
	
	public void setActive(Set<Link> m) {
		activeLinks = m;
		topoOrder = null;

	}
}
