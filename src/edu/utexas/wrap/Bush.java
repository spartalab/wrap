package edu.utexas.wrap;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Bush extends Graph implements AssignmentContainer  {

	// Bush structure
	private final Origin origin;
	private final Float vot;
	private final VehicleClass c;

	
	// Back vector maps
	private Map<Integer, Link> 		qShort;
	private Map<Integer, Link>		qLong;

	private LinkedList<Node>		topoOrder;


	public Bush(Origin o, Map<Integer,Node> nodes, Set<Link> links, Float vot, Map<Integer, Float> destDemand, VehicleClass c) {
		super();
		origin = o;
		this.vot = vot;
		this.c = c;

		//Initialize flow and status maps

		this.nodeMap	= nodes;
		qShort	= origin.getInitMap(nodes);//new HashMap<Integer, Link>(nodes.size(),1.0f);
		qLong	= new HashMap<Integer, Link>(nodes.size(),1.0f);

		//		runDijkstras();
		dumpFlow(destDemand);
	}

	public Bush(Origin o, Graph g, Float vot, Map<Integer, Float> destDemand, VehicleClass c) {
		this(o, g.getNodeMap(), g.getLinks(), vot, destDemand, c);
	}

	public void changeFlow(Link l, Double delta) {
		if (l.alterBushFlow(delta, this)) activate(l);
		else deactivate(l);
	}

	/**Initialize demand flow on shortest paths
	 * Add each destination's demand to the shortest path to that destination
	 * @param destDemand 
	 * */
	private void dumpFlow(Map<Integer, Float> destDemand) {
		for (Integer node : nodeMap.keySet()) {


			Float x = destDemand.getOrDefault(node, 0.0F);
			if (x <= 0.0) continue;
			Path p;
			try {
				p = getShortPath(nodeMap.get(node));
			} catch (UnreachableException e) {
				// TODO Auto-generated catch block
				System.err.println("No path exists from Node "+origin.getID()+" to Node "+node+". Lost demand = "+x);
				//				destDemand.put(node, 0.0F);
				continue;
			}
			for (Link l : p) {
				changeFlow(l, (double) x);
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
		return (topoOrder != null) ?  topoOrder :  generateTopoOrder();
	}

	/**
	 * @return Nodes in topological order
	 */
	private LinkedList<Node> generateTopoOrder() {
		// Start with a set of all bush edges
		Set<Link> currentLinks = getLinks();

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
		if (!currentLinks.isEmpty()) throw new RuntimeException("Cyclic graph error");
		topoOrder = to;
		return to;
	}


	public boolean isInvalidConnector(Link uv) {
		if (!(uv instanceof CentroidConnector) || 
				uv.getTail().equals(origin) || 
				(uv.getHead().isCentroid() && !uv.getTail().isCentroid())
				) return false;
		else return true;
	}

	/**Calculate shortest or longest paths in bush (DAG) using topological search
	 * 
	 * Leverage the presence of a topological order to decrease search time for 
	 * shortest/longest paths calculation
	 * 
	 * @param longest switch for Longest/Shortest
	 * @param to a topological ordering of the nodes
	 */
	public Map<Node, Double> topoSearch(Boolean longest)  {
		// Initialize all nodeU values as 0 and all nodes as not visited
		List<Node> to = getTopologicalOrder();
		Map<Node, Double> cache = new HashMap<Node, Double>(nodeMap.size());
		//SHORTEST PATHS
		if(!longest) {
			//Initialize infinity-filled nodeL and empty qShort
			qShort = new HashMap<Integer, Link>(nodeMap.size(),1.0f);
			for (Node d : to) {
				try {
					for (Link l :  outLinks(d)) {
						Double Licij = l.getPrice(vot,c) + getCachedL(d,cache);

						Node head = l.getHead();
						Integer id = head.getID();
						if (qShort.get(id) == null || Licij < getCachedL(head,cache)) {
							qShort.put(id, l);
							cache.put(head, Licij);
						}					
					}
				} catch (UnreachableException e) {
					if (getDemand(d.getID()) > 0.0) {
						throw new RuntimeException();
					}
				}
			}
		}

		//LONGEST PATHS
		else  {
			qLong = new HashMap<Integer, Link>(nodeMap.size(),1.0f);
			for (Node d : to) {
				try {
					for (Link l : outLinks(d)) {

						Double Uicij = l.getPrice(vot,c) + getCachedU(d,cache);
						Node head = l.getHead();
						Integer id = l.getHead().getID();
						if (qLong.get(id) == null || Uicij > getCachedU(head,cache)) {
							qLong.put(id, l);
							cache.put(head, Uicij);
						}

					}
				} catch (UnreachableException e) {
					if (getDemand(d.getID()) > 0.0) {
						throw new RuntimeException();
					}
				}
			}
		}
		return cache;
	}


	public Link getqShort(Node n) {
		return qShort.get(n.getID());
	}

	public Link getqLong(Node n) {
		return qLong.get(n.getID());
	}

	public Path getShortPath(Node n) throws UnreachableException {
		return getShortPath(n, origin);
	}

	public Path getShortPath(Node end, Node start) throws UnreachableException {
		Path p = new Path();
		if (end.equals(start)) return p;
		Link curLink = getqShort(end);
		while (curLink != null && !curLink.getHead().equals(start)) {
			p.addFirst(curLink);
			curLink = getqShort(curLink.getTail());	
		}
		if (p.isEmpty() || !p.getFirst().getTail().equals(start)) throw new UnreachableException();
		return p;
	}

	public Path getLongPath(Node n) {
		return getLongPath(n,origin);
	}

	public Path getLongPath(Node end, Node start) {

		Path p = new Path();
		if (end.equals(start)) return p;
		Link curLink = getqLong(end);
		while (curLink != null && !curLink.getHead().equals(start)) {
			p.addFirst(curLink);
			curLink = getqLong(curLink.getTail());
		}
		return p;

	}

	public Double getU(Node n) throws UnreachableException {

		Link back = qLong.get(n.getID());
		if (n.equals(origin)) return 0.0;
		else if (back == null) throw new UnreachableException(n,this);
		else return getU(back.getTail()) + back.getPrice(vot,c);

	}

	public Double getL(Node n) throws UnreachableException {

		Link back = qShort.get(n.getID());
		if (n.equals(origin)) return 0.0;
		else if (back == null) throw new UnreachableException(n,this);
		else return getL(back.getTail()) + back.getPrice(vot,c);
	}

	public Double getCachedU(Node n, Map<Node, Double> cache) throws UnreachableException {
		Link back = qLong.get(n.getID());
		if (n.equals(origin)) return 0.0;
		else if (back == null) throw new UnreachableException(n, this);
		else if (cache.containsKey(n)) return cache.get(n);
		else {
			Double newU = getCachedU(back.getTail(),cache) + back.getPrice(vot,c);
			cache.put(n, newU);
			return newU;
		}
	}

	public Double getCachedL(Node n, Map<Node, Double> cache) throws UnreachableException {
		Link back = qShort.get(n.getID());
		if (n.equals(origin)) return 0.0;
		else if (back == null)
			throw new UnreachableException(n,this);
		else if (cache.containsKey(n)) return cache.get(n);
		else {
			Double newL = getCachedL(back.getTail(), cache) + back.getPrice(vot,c);
			cache.put(n,newL);
			return newL;
		}
	}


	public Node getOrigin() {
		return origin;
	}

	public Collection<Node> getNodes() {
		return nodeMap.values();
	}

	public Float getVOT() {
		return vot;
	}

	public Float getDemand(Integer n) {
		Node node = nodeMap.get(n);
		Double inFlow = 0.0;
		for (Link l : node.getIncomingLinks()) {
			inFlow += l.getBushFlow(this);
		}
		Double outFlow = 0.0;
		for (Link l : node.getOutgoingLinks()) {
			outFlow += l.getBushFlow(this);
		}
		return (float) (inFlow - outFlow);
	}

	public String toString() {
		return "ORIG="+origin.getID()+"\tVOT="+vot+"\tCLASS="+c;
	}

	void activate(Link l) {
		if (add(l)) topoOrder = null;
	}

	private void markInactive(Link l) {
		if (remove(l)) {
			topoOrder = null;
		}
	}

	Boolean deactivate(Link l) {
		Node head = l.getHead();
		for (Link i : head.getIncomingLinks()) {
			if (contains(i) && !i.equals(l)) {
				markInactive(l);
				return true;
			}
		}
		return false;
	}

	void prune() {
		for (Link l : getLinks()){
			if(!l.hasFlow(this)){
				// Check to see if this link is needed for connectivity, deactivate link in bush if no flow left
				deactivate(l);
			}

		}
	}

	//	private Integer depthL(Node n) {
	//		if (n.equals(origin)) return 0;
	//		return depthL(getqShort(n).getTail())+1;
	//	}
	//	
	//	private Integer depthU(Node n) {
	//		if (n.equals(origin)) return 0;
	//		return depthU(getqLong(n).getTail())+1;
	//	}
	//	
	Node divergeNode(Node l, Node u) {
		if (l.equals(origin) || u.equals(origin)) return origin;

		Path lPath;
		try {
			lPath = getShortPath(l);
		} catch (UnreachableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		Path uPath = getLongPath(u);

		Set<Node> lNodes = new HashSet<Node>();
		Set<Node> uNodes = new HashSet<Node>();

		Iterator<Link> lIter = lPath.descendingIterator();
		Iterator<Link> uIter = uPath.descendingIterator();

		Link uLink, lLink;
		while (lIter.hasNext() && uIter.hasNext()) {
			lLink = lIter.next();
			uLink = uIter.next();

			if (lLink.getTail().equals(uLink.getTail())) return lLink.getTail();
			else if (uNodes.contains(lLink.getTail())) return lLink.getTail();
			else if (lNodes.contains(uLink.getTail())) return uLink.getTail();
			else {
				lNodes.add(lLink.getTail());
				uNodes.add(uLink.getTail());
			}
		}
		while (uIter.hasNext()) {
			uLink = uIter.next();
			if (lNodes.contains(uLink.getTail())) return uLink.getTail();
		}
		while (lIter.hasNext()) {
			lLink = lIter.next();
			if (uNodes.contains(lLink.getTail())) return lLink.getTail();
		}
		return null;
	}

	public AlternateSegmentPair getShortLongASP(Node terminus) {
		Link shortLink = getqShort(terminus);
		Link longLink = getqLong(terminus);

		// If there is no divergence node, move on to the next topological node
		if (longLink.equals(shortLink)) return null;

		//Else calculate divergence node

		Node diverge = divergeNode(shortLink.getTail(), longLink.getTail());
		try {
			return new AlternateSegmentPair(getShortPath(terminus, diverge), getLongPath(terminus,diverge), this);
		} catch (UnreachableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public VehicleClass getVehicleClass() {
		return c;
	}
}
