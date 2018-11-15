package edu.utexas.wrap;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Bush extends Graph implements AssignmentContainer {

	// Bush structure
	private final Origin origin;
	private final Float vot;
	private final VehicleClass c;

	private final Graph wholeNet;

	// Back vector maps
	private Map<Node, Link> qShort;
	private Map<Node, Link> qLong;

	private LinkedList<Node> cachedTopoOrder;

	public Bush(Origin o, Graph g, Float vot, Map<Node, Float> destDemand, VehicleClass c) {
		super();
		origin = o;
		this.vot = vot;
		this.c = c;

		// Initialize flow and status maps
		wholeNet = g;
		qShort = origin.getInitMap(g);
		addAll(qShort.values());
		qLong = new HashMap<Node, Link>(wholeNet.numNodes(), 1.0f);

		dumpFlow(destDemand);
	}

	void activate(Link l) {
		if (add(l))
			cachedTopoOrder = null;
	}

	public void changeFlow(Link l, Double delta) {
		if (l.alterBushFlow(delta, this))
			activate(l);
		else
			deactivate(l);
	}

	private Boolean deactivate(Link l) {
		Node head = l.getHead();
		for (Link i : inLinks(head)) {
			if (!i.equals(l)) {
				markInactive(l);
				return true;
			}
		}
		return false;
	}

	Node divergeNode(Node l, Node u) {
		if (l.equals(origin.getNode()) || u.equals(origin.getNode()))
			return origin.getNode();

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

			if (lLink.getTail().equals(uLink.getTail()))
				return lLink.getTail();
			else if (uNodes.contains(lLink.getTail()))
				return lLink.getTail();
			else if (lNodes.contains(uLink.getTail()))
				return uLink.getTail();
			else {
				lNodes.add(lLink.getTail());
				uNodes.add(uLink.getTail());
			}
		}
		while (uIter.hasNext()) {
			uLink = uIter.next();
			if (lNodes.contains(uLink.getTail()))
				return uLink.getTail();
		}
		while (lIter.hasNext()) {
			lLink = lIter.next();
			if (uNodes.contains(lLink.getTail()))
				return lLink.getTail();
		}
		return null;
	}

	/**
	 * Initialize demand flow on shortest paths Add each destination's demand to the
	 * shortest path to that destination
	 * 
	 * @param destDemand
	 */
	private void dumpFlow(Map<Node, Float> destDemand) {
		for (Node node : destDemand.keySet()) {

			Float x = destDemand.getOrDefault(node, 0.0F);
			if (x <= 0.0)
				continue;
			Path p;
			try {
				p = getShortPath(node);
			} catch (UnreachableException e) {
				System.err.println("No path exists from Node " + origin.getNode().getID() + " to Node " + node
						+ ". Lost demand = " + x);
				continue;
			}
			for (Link l : p) {
				changeFlow(l, (double) x);
			}
		}

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
		S.add(origin.getNode());
		Node n;

		while (!S.isEmpty()) {
			n = S.pop();// remove node from S
			to.add(n); // append node to L

			// for each active edge out of this node
			for (Link l : outLinks(n)) {
				if (currentLinks.contains(l)) {

					// remove the links from the set
					currentLinks.remove(l);
					// the node on the other end
					Node m = l.getHead();

					// see if this node has no other incoming active links
					boolean mHasIncoming = false;
					for (Link e : inLinks(m)) {
						if (currentLinks.contains(e)) {
							mHasIncoming = true;
							break;
						}
					}
					// if not, add to the list of start nodes
					if (!mHasIncoming)
						S.add(m);
				}
			}
		}
		if (!currentLinks.isEmpty())
			throw new RuntimeException("Cyclic graph error");
		cachedTopoOrder = to;
		return to;
	}

	public Double getCachedL(Node n, Map<Node, Double> cache) throws UnreachableException {
		Link back = qShort.get(n);
		if (n.equals(origin.getNode()))
			return 0.0;
		else if (back == null)
			throw new UnreachableException(n, this);
		else if (cache.containsKey(n))
			return cache.get(n);
		else {
			Double newL = getCachedL(back.getTail(), cache) + back.getPrice(vot, c);
			cache.put(n, newL);
			return newL;
		}
	}

	public Double getCachedU(Node n, Map<Node, Double> cache) throws UnreachableException {
		Link back = qLong.get(n);
		if (n.equals(origin.getNode()))
			return 0.0;
		else if (back == null)
			throw new UnreachableException(n, this);
		else if (cache.containsKey(n))
			return cache.get(n);
		else {
			Double newU = getCachedU(back.getTail(), cache) + back.getPrice(vot, c);
			cache.put(n, newU);
			return newU;
		}
	}

	public Float getDemand(Node node) {
		Double inFlow = 0.0;
		for (Link l : inLinks(node)) {
			inFlow += l.getBushFlow(this);
		}
		Double outFlow = 0.0;
		for (Link l : outLinks(node)) {
			outFlow += l.getBushFlow(this);
		}
		return (float) (inFlow - outFlow);
	}

	public Double getL(Node n) throws UnreachableException {

		Link back = qShort.get(n);
		if (n.equals(origin.getNode()))
			return 0.0;
		else if (back == null)
			throw new UnreachableException(n, this);
		else
			return getL(back.getTail()) + back.getPrice(vot, c);
	}

	public Path getLongPath(Node n) {
		return getLongPath(n, origin.getNode());
	}

	public Path getLongPath(Node end, Node start) {

		Path p = new Path();
		if (end.equals(start))
			return p;
		Link curLink = getqLong(end);
		while (curLink != null && !curLink.getHead().equals(start)) {
			p.addFirst(curLink);
			curLink = getqLong(curLink.getTail());
		}
		return p;

	}

	public Collection<Node> getNodes() {
		return wholeNet.getNodes();
	}

	public Origin getOrigin() {
		return origin;
	}

	public Link getqLong(Node n) {
		return qLong.get(n);
	}

	public Link getqShort(Node n) {
		return qShort.get(n);
	}

	public AlternateSegmentPair getShortLongASP(Node terminus) {
		Link shortLink = getqShort(terminus);
		Link longLink = getqLong(terminus);

		// If there is no divergence node, move on to the next topological node
		if (longLink.equals(shortLink))
			return null;

		// Else calculate divergence node

		Node diverge = divergeNode(shortLink.getTail(), longLink.getTail());
		try {
			return new AlternateSegmentPair(getShortPath(terminus, diverge), getLongPath(terminus, diverge), this);
		} catch (UnreachableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public Path getShortPath(Node n) throws UnreachableException {
		return getShortPath(n, origin.getNode());
	}

	public Path getShortPath(Node end, Node start) throws UnreachableException {
		Path p = new Path();
		if (end.equals(start))
			return p;
		Link curLink = getqShort(end);
		while (curLink != null && !curLink.getHead().equals(start)) {
			p.addFirst(curLink);
			curLink = getqShort(curLink.getTail());
		}
		if (p.isEmpty() || !p.getFirst().getTail().equals(start))
			throw new UnreachableException();
		return p;
	}

	/**
	 * Calculate a topological order using Kahn's algorithm
	 * 
	 * Evaluate the set of bush links, starting from the origin and determine a
	 * topological order for the nodes that they attach
	 * 
	 * @return a topological ordering of this bush's nodes
	 */
	public LinkedList<Node> getTopologicalOrder() {
		return (cachedTopoOrder != null) ? cachedTopoOrder : generateTopoOrder();
	}

	public Double getU(Node n) throws UnreachableException {

		Link back = qLong.get(n);
		if (n.equals(origin.getNode()))
			return 0.0;
		else if (back == null)
			throw new UnreachableException(n, this);
		else
			return getU(back.getTail()) + back.getPrice(vot, c);

	}

	public VehicleClass getVehicleClass() {
		return c;
	}

	public Float getVOT() {
		return vot;
	}

	public boolean isInvalidConnector(Link uv) {
		if (!(uv instanceof CentroidConnector) || uv.getTail().equals(origin.getNode())
				|| (uv.getHead().isCentroid() && !uv.getTail().isCentroid()))
			return false;
		else
			return true;
	}

	private void longRelax(Link l, Map<Node, Double> cache) throws UnreachableException {
		Double Uicij = l.getPrice(vot, c) + getCachedU(l.getTail(), cache);
		Node head = l.getHead();
		if (qLong.get(head) == null || Uicij > getCachedU(head, cache)) {
			qLong.put(head, l);
			cache.put(head, Uicij);
		}
	}

	/**
	 * Calculate longest paths in bush (DAG) using topological search
	 * 
	 * Leverage the presence of a topological order to decrease search time for
	 * longest paths calculation
	 */
	public Map<Node, Double> longTopoSearch() {
		List<Node> to = getTopologicalOrder();
		Map<Node, Double> cache = new HashMap<Node, Double>(wholeNet.numNodes());

		qLong = new HashMap<Node, Link>(wholeNet.numNodes(), 1.0f);
		for (Node d : to) {
			try {
				for (Link l : outLinks(d)) {
					longRelax(l, cache);
				}
			} catch (UnreachableException e) {
				if (getDemand(d) > 0.0) {
					throw new RuntimeException();
				}
			}
		}
		return cache;
	}

	private void markInactive(Link l) {
		if (remove(l)) {
			cachedTopoOrder = null;
		}
	}

	void prune() {
		for (Link l : getLinks()) {
			if (!l.hasFlow(this)) {
				// Check to see if this link is needed for connectivity, deactivate link in bush
				// if no flow left
				deactivate(l);
			}

		}
	}

	private void shortRelax(Link l, Map<Node, Double> cache) throws UnreachableException {
		Double Licij = l.getPrice(vot, c) + getCachedL(l.getTail(), cache);

		Node head = l.getHead();
		if (qShort.get(head) == null || Licij < getCachedL(head, cache)) {
			qShort.put(head, l);
			cache.put(head, Licij);
		}
	}

	/**
	 * Calculate shortest paths in bush (DAG) using topological search
	 * 
	 * Leverage the presence of a topological order to decrease search time for
	 * shortest paths calculation
	 */
	public Map<Node, Double> shortTopoSearch() {
		List<Node> to = getTopologicalOrder();
		Map<Node, Double> cache = new HashMap<Node, Double>(wholeNet.numNodes());

		qShort = new HashMap<Node, Link>(wholeNet.numNodes(), 1.0f);
		for (Node d : to) {
			try {
				for (Link l : outLinks(d)) {
					shortRelax(l, cache);
				}
			} catch (UnreachableException e) {
				if (getDemand(d) > 0.0) {
					throw new RuntimeException();
				}
			}
		}
		return cache;
	}

	public String toString() {
		return "ORIG=" + origin.getNode().getID() + "\tVOT=" + vot + "\tCLASS=" + c;
	}
}
