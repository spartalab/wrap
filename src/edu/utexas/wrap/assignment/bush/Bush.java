package edu.utexas.wrap.assignment.bush;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import edu.utexas.wrap.assignment.AssignmentContainer;
import edu.utexas.wrap.assignment.Path;
import edu.utexas.wrap.demand.containers.*;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.CentroidConnector;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.Node;
import edu.utexas.wrap.util.AlternateSegmentPair;
import edu.utexas.wrap.util.UnreachableException;

/** An instance of a {@link edu.utexas.wrap.assignment.AssignmentContainer}
 * that is used for bush-based assignment methods.
 * @author William
 *
 */
public class Bush implements AssignmentContainer {

	// Bush structure
	private final BushOrigin origin;
	private final Float vot;
	private final Mode c;

	private final Graph wholeNet;
	private final DemandHashMap demand;

	// Back vector maps
	private Map<Node, BackVector> q;
//	private Map<Node, Link> qShort;
//	private Map<Node, Link> qLong;


	private LinkedList<Node> cachedTopoOrder;

	public Bush(BushOrigin o, Graph g, Float vot, DemandHashMap destDemand, Mode c) {
		origin = o;
		this.vot = vot;
		this.c = c;

		// Initialize flow and status maps
		wholeNet = g;
		q = new HashMap<Node,BackVector>(origin.getInitMap(g));
//		qLong = new HashMap<Node, Link>(g.numNodes(), 1.0f);
		demand = destDemand;
		dumpFlow(demand);
	}

	void activate(Link l) {
		if (add(l))
			cachedTopoOrder = null;
	}

	/** Add a link to the bush
	 * @param l the link to be added
	 * @return whether the link was successfully added to the bush
	 */
	private boolean add(Link l) {
		Node head = l.getHead();
		BackVector prior = q.get(head);
		// If there was no backvector (should only happen during bush initialization), add link
		if (prior == null) {
			q.put(head, l);
			return true;
		}
		// If the link is already the sole backvector, do nothing
		else if (prior.equals(l)) return false;
		// If the head node already has a merge backvector, delegate 
		else if (prior instanceof BushMerge) {
			return ((BushMerge) prior).add(l);
		}
		// Else, we need to create a new merge backvector to replace the current back link
		else if (prior instanceof Link) { // If we're here, the back link needs to be replaced by a merge
			BushMerge nuevo = new BushMerge();
			nuevo.add((Link) prior);
			nuevo.add(l);
			q.put(head, nuevo);
			return true;
		}

		return false;
	}

	public void changeFlow(Link l, Double delta) {
		if (l.alterBushFlow(delta))
			activate(l);
		else
			deactivate(l);
	}

	private Boolean deactivate(Link l) {
		Node head = l.getHead();
		for (Link i : wholeNet.inLinks(head)) {
			if (contains(i) && !i.equals(l)) {
				markInactive(l);
				return true;
			}
		}
		return false;
	}

	private boolean contains(Link i) {
		BackVector back = q.get(i.getHead());
		return back instanceof BushMerge? ((BushMerge) back).contains(i) : back.equals(i);

	}

	Node divergeNode(Node l, Node u) {
		if (l.equals(origin.getNode()) || u.equals(origin.getNode()))
			return origin.getNode();

		Path lPath;
		try {
			lPath = getShortPath(l);
		} catch (UnreachableException e) {
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
		//TODO redo this method
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
			for (Link l : wholeNet.outLinks(n)) {
				if (currentLinks.contains(l)) {

					// remove the links from the set
					currentLinks.remove(l);
					// the node on the other end
					Node m = l.getHead();

					// see if this node has no other incoming active links
					boolean mHasIncoming = false;
					for (Link e : wholeNet.inLinks(m)) {
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
//		cachedTopoOrder = to;
		return to;
	}

	public Double getCachedL(Node n, Map<Node, Double> cache) throws UnreachableException {
		Link back = getqShort(n);
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
		Link back = getqShort(n);
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
		return demand.get(node);
	}
	
	public Double getL(Node n) throws UnreachableException {

		Link back = getqShort(n);
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

	public BushOrigin getOrigin() {
		return origin;
	}

	public Link getqLong(Node n) {
		Object qq = q.get(n);
		return qq instanceof Link? (Link) qq :
			qq instanceof BushMerge? ((BushMerge) qq).getLongLink() :
				null;
	}

	public Link getqShort(Node n) {
		Object qq = q.get(n);
		return qq instanceof Link? (Link) qq :
			qq instanceof BushMerge ? ((BushMerge) qq).getShortLink() :
				null;
	}

	public AlternateSegmentPair getShortLongASP(Node terminus) {
		//Iterate through longest paths until reaching a node in shortest path

		//Reiterate through shortest path to build path up to divergence node
		//The two paths constitute a Pair of Alternate Segments

		
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

		Link back = getqLong(n);
		if (n.equals(origin.getNode()))
			return 0.0;
		else if (back == null)
			throw new UnreachableException(n, this);
		else
			return getU(back.getTail()) + back.getPrice(vot, c);

	}

	public Mode getVehicleClass() {
		return c;
	}

	public Float getVOT() {
		return vot;
	}
	
	@Override
	public int hashCode() {
		//origin;vot;c;
		int a = 2017;	//	Year of inception for this project
		int b = 76537;	//	UT 76-5-37 TAMC \m/
		
		return (origin.getNode().getID()*a + vot.intValue())*b + (c == null? 0 : c.hashCode());
//		return hc;
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
		if (getqLong(head) == null || Uicij > getCachedU(head, cache)) {
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

		qLong = new Object2ObjectOpenHashMap<Node, Link>(wholeNet.numNodes(), 1.0f);
		for (Node d : to) {
			try {
				for (Link l : wholeNet.outLinks(d)) {
					if (contains(l)) longRelax(l, cache);
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

	private boolean remove(Link l) {
		BackVector b = q.get(l.getHead());
		// If the link can't be removed due to connectivity, return false
		if (b instanceof Link && ((Link) b).equals(l)) return false;
		// If there was a merge present at the head node, attempt to remove the link from it
		else if (b instanceof BushMerge) {
			return ((BushMerge) b).remove(l);
		}
		// If something unusual happened, throw a Runtime exception
		else throw new RuntimeException("A link was removed that wasn't in the bush");
	}

	void prune() {
		// TODO optimize for new bush structure
		for (Link l : getLinks()) {
			if (getFlow(l) == 0.0) {
				// Check to see if this link is needed for connectivity, deactivate link in bush
				// if no flow left
				deactivate(l);
			}

		}
	}

	public Double getFlow(Link l) {
		// TODO Auto-generated method stub
		return 0.0;
	}

	private void shortRelax(Link l, Map<Node, Double> cache) throws UnreachableException {
		Double Licij = l.getPrice(vot, c) + getCachedL(l.getTail(), cache);

		Node head = l.getHead();
		if (getqShort(head) == null || Licij < getCachedL(head, cache)) {
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

		qShort = new Object2ObjectOpenHashMap<Node, Link>(wholeNet.numNodes(), 1.0f);
		for (Node d : to) {
			try {
				for (Link l : wholeNet.outLinks(d)) {
					if (contains(l)) shortRelax(l, cache);
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

	@Override
	public Set<Link> getLinks() {
		Set<Link> ret = new HashSet<Link>();
		for (BackVector b : q.values()) {
			if (b instanceof Link) ret.add((Link) b);
			else if (b instanceof BushMerge) ret.addAll((BushMerge) b);
		}
		return ret;

	}
}
