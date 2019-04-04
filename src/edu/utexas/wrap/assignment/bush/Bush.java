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
import edu.utexas.wrap.demand.DemandMap;
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
	private final DemandMap demand;

	// Back vector maps
	private Map<Node, BackVector> q;

	private LinkedList<Node> cachedTopoOrder;

	public Bush(BushOrigin o, Graph g, Float vot, DemandMap destDemand, Mode c) {
		origin = o;
		this.vot = vot;
		this.c = c;

		// Initialize flow and status maps
		wholeNet = g;
		q = new HashMap<Node,BackVector>(origin.getInitMap(g));
		demand = destDemand;
		dumpFlow();
	}

	/**Attempt to add the bush to the link
	 * @param l the link to be added
	 */
	void activate(Link l) {
		if (add(l)) // If the bush structure changed, reset the topological order
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
			BushMerge nuevo = new BushMerge(this, (Link) prior, l);
			q.put(head, nuevo);
			return true;
		}
		//Something went wrong - there was no backvector
		throw new RuntimeException("No backvector found");
	}

	/**Modify the flow on a link from this bush
	 * @param l the link whose flow will be modified
	 * @param delta the amount by which to modify
	 */
	public void changeFlow(Link l, Double delta) {
		//TODO check to make sure we don't remove more flow than the bush uses
		//Modify the flow on the link
		l.changeFlow(delta);
		Double flow = getFlow(l);
		//If there is very close to zero flow
		if (flow <= Math.ulp(flow)*2) {
			//Try to deactivate it. If successful, remove microscopic flow from link
			if (deactivate(l)) l.changeFlow(-flow);			
		}
		//Otherwise ensure the link is active in the bush
		else activate(l);
		return 7;//Leaving this in here until all flow changes are complete
	}

	/**Attempt to remove the bush from the link
	 * @param l the link to be removed
	 * @return whether the link was successfully removed from the bush
	 */
	private Boolean deactivate(Link l) {
		BackVector b = q.get(l.getHead());
		//If there is only one link as the backvector, it can't be removed
		if (b instanceof BushMerge && remove(l)) {
			//But if it can successfully be removed from the BushMerge, reset the topological ordering
			cachedTopoOrder = null;
			return true;
		}
		return false;
	}

	/**Determine whether a link is active in the bush
	 * @param i the link which should be checked
	 * @return whether the given link is in the bush structure
	 */
	private boolean contains(Link i) {
		BackVector back = q.get(i.getHead());
		//Look at the head node's backvector
		return back instanceof BushMerge? 
				//If it's a merge, determine if the merge contains the link
				((BushMerge) back).contains(i) : 
					//Otherwise, determine if the back vector is the same link
					back.equals(i);
	}

	/** Calculates the divergence between the shortest and longest paths from two nodes
	 * @param l the node from which the shortest path should trace back
	 * @param u the node from which the longest path should trace back
	 * @return the diverge node
	 */
	protected Node divergeNode(Node start) {
		// If the given node is the origin or it has only one backvector, there is no diverge node
		if (start.equals(origin.getNode()) || !(q.get(start) instanceof BushMerge))
			return start;

		//Assemble the shortest path to the origin
		Path lPath;
		try {
			lPath = getShortPath(start);
		} catch (UnreachableException e) {
			e.printStackTrace();
			return null;
		}
		//Assemble the longest path to the origin
		Path uPath = getLongPath(start);

		//Store the nodes seen on the paths in reverse order
		Set<Node> lNodes = new HashSet<Node>();
		Set<Node> uNodes = new HashSet<Node>();
		Iterator<Link> lIter = lPath.descendingIterator();
		Iterator<Link> uIter = uPath.descendingIterator();

		//Iterate backwards through the shortest and longest paths until one runs out of links
		Link uLink, lLink;
		while (lIter.hasNext() && uIter.hasNext()) {
			lLink = lIter.next();
			uLink = uIter.next();

			//If the next node in both is the same, return that node
			if (lLink.getTail().equals(uLink.getTail()))
				return lLink.getTail();
			//If the shortest path just got to a node that was already seen in the longest path, return it
			else if (uNodes.contains(lLink.getTail()))
				return lLink.getTail();
			//If the longest path just got to a node that was already seen in the shortest path, return it
			else if (lNodes.contains(uLink.getTail()))
				return uLink.getTail();
			//Otherwise, add both to the lists of seen nodes
			else {
				lNodes.add(lLink.getTail());
				uNodes.add(uLink.getTail());
			}
		}
		//If there are still more links to examine on the longest path, do so
		while (uIter.hasNext()) {
			uLink = uIter.next();
			//If the longest path just got to a node that was already seen in the shortest path, return it
			if (lNodes.contains(uLink.getTail()))
				return uLink.getTail();
		}
		//If there are still more links to examine on the shortest path, do so
		while (lIter.hasNext()) {
			lLink = lIter.next();
			//If the shortest path just got to a node that was already seen in the longest path, return it
			if (uNodes.contains(lLink.getTail()))
				return lLink.getTail();
		}
		//Something went wrong - the two paths never intersected
		throw new RuntimeException("No diverge node found");
	}

	/**
	 * Initialize demand flow on shortest paths Add each destination's demand to the
	 * shortest path to that destination
	 * 
	 * @param destDemand
	 */
	private void dumpFlow() {
		//TODO redo this method
		for (Node node : demand.getNodes()) {

			Float x = demand.getOrDefault(node, 0.0F);
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

	/**Generate a topological ordering from scratch for this bush
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

	/**Recursively calculate the shortest path cost to a node using a cache
	 * @param n the node to calculate the cost of the shortest path to
	 * @param cache the cache used to avoid recomputing
	 * @return the cost of the shortest path to the given node
	 * @throws UnreachableException if a node can't be reached
	 */
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

	/**Recursively calculate the longest path cost to a node using a cache
	 * @param n the node to calculate the cost of the longest path to
	 * @param cache the cache used to avoid recomputing
	 * @return the cost of the longest path to the given node
	 * @throws UnreachableException if a node can't be reached
	 */
	public Double getCachedU(Node n, Map<Node, Double> cache) throws UnreachableException {
		Link back = getqLong(n);
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

	/* (non-Javadoc)
	 * @see edu.utexas.wrap.assignment.AssignmentContainer#getDemand(edu.utexas.wrap.net.Node)
	 */
	public Float getDemand(Node node) {
		return demand.get(node);
	}
	
	/**Recursively calculate the shortest path cost to a node
	 * @param n the node to calculate the cost of the shortest path to
	 * @return the cost of the shortest path to the given node
	 * @throws UnreachableException if a node can't be reached
	 */
	public Double getL(Node n) throws UnreachableException {

		Link back = getqShort(n);
		if (n.equals(origin.getNode()))
			return 0.0;
		else if (back == null)
			throw new UnreachableException(n, this);
		else
			return getL(back.getTail()) + back.getPrice(vot, c);
	}

	/**Assemble the longest path from the origin to a node
	 * @param n the node to assemble the longest path to
	 * @return the longest path to that node
	 */
	public Path getLongPath(Node n) {
		return getLongPath(n, origin.getNode());
	}

	/**Assemble the longest path from a given start node to an end node
	 * @param end the node to assemble the longest path to
	 * @param start the node to assemble the longest path from
	 * @return the longest path from the start to the end node
	 */
	public Path getLongPath(Node end, Node start) {

		Path p = new Path();
		if (end.equals(start))
			return p;
		Link curLink = getqLong(end);
		while (curLink != null && !curLink.getHead().equals(start)) {
			p.addFirst(curLink);
			curLink = getqLong(curLink.getTail());
		}
		if (curLink == null && !start.equals(origin.getNode())) 
			throw new RuntimeException("No longest path could be found from "+start.toString()+" to "+end.toString());
		return p;

	}

	/**Get all the nodes used in the bush
	 * @return all nodes used in the bush
	 */
	public Collection<Node> getNodes() {
		return wholeNet.getNodes();
	}

	/**Get the bush´s origin
	 * @return the origin of the bush
	 */
	public BushOrigin getOrigin() {
		return origin;
	}

	/**Get the longest path backvector from a given node
	 * @param n the node whose longest path backvector should be returned
	 * @return the last link in the longest path to the given node
	 */
	public Link getqLong(Node n) {
		BackVector qq = q.get(n);
		return qq instanceof Link? (Link) qq :
			qq instanceof BushMerge? ((BushMerge) qq).getLongLink() :
				null;
	}

	/**Get the shortest path backvector from a given node
	 * @param n the node whose shortest path backvector should be returned
	 * @return the last link in the shortest path to the given node
	 */
	public Link getqShort(Node n) {
		BackVector qq = q.get(n);
		return qq instanceof Link? (Link) qq :
			qq instanceof BushMerge ? ((BushMerge) qq).getShortLink() :
				null;
	}

	/**Assemble the alternate segment pair of longest and shortest paths emanating from the terminus
	 * @param terminus the node whose longest-shortest ASP should be calculated
	 * @return the alternate segment pair consisting of the shortest and longest paths to the terminus
	 */
	public AlternateSegmentPair getShortLongASP(Node terminus) {
		//Iterate through longest paths until reaching a node in shortest path

		//Reiterate through shortest path to build path up to divergence node
		//The two paths constitute a Pair of Alternate Segments
		
		Link shortLink = getqShort(terminus);
		Link longLink = getqLong(terminus);

		// If there is no divergence node, move on to the next topological node
		if (!(q.get(terminus) instanceof BushMerge) || longLink.equals(shortLink))
			return null;

		// Else calculate divergence node
		Node diverge = divergeNode(terminus);
		try {
			return new AlternateSegmentPair(getShortPath(terminus, diverge), getLongPath(terminus, diverge), this);
		} catch (UnreachableException e) {
			e.printStackTrace();
			return null;
		}
	}

	/** Assemble the shortest path from the origin to a given node
	 * @param n the final node in the path
	 * @return the shortest path between the origin and given node
	 * @throws UnreachableException if a shortest path can't be constructed
	 */
	public Path getShortPath(Node n) throws UnreachableException {
		return getShortPath(n, origin.getNode());
	}

	/** Assemble the shortest path from the start to the end node
	 * @param end the final node in the path
	 * @param start the first node in the path
	 * @return the shortest path between the start and end nodes
	 * @throws UnreachableException if a shortest path can't be constructed
	 */
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

	/**Recursively calculate the longest path to a node
	 * @param n the node to calculate cost of the longest path to
	 * @return the cost of the longest path to the node
	 * @throws UnreachableException if a node can't be reached
	 */
	public Double getU(Node n) throws UnreachableException {

		Link back = getqLong(n);
		if (n.equals(origin.getNode()))
			return 0.0;
		else if (back == null)
			throw new UnreachableException(n, this);
		else
			return getU(back.getTail()) + back.getPrice(vot, c);

	}

	/* (non-Javadoc)
	 * @see edu.utexas.wrap.assignment.AssignmentContainer#getVehicleClass()
	 */
	public Mode getVehicleClass() {
		return c;
	}

	/* (non-Javadoc)
	 * @see edu.utexas.wrap.assignment.AssignmentContainer#getVOT()
	 */
	public Float getVOT() {
		return vot;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		//origin;vot;c;
		int a = 2017;	//	Year of inception for this project
		int b = 76537;	//	UT 76-5-37 TAMC \m/
		
		return (origin.getNode().getID()*a + vot.intValue())*b + (c == null? 0 : c.hashCode());
	}
	
	/**Determine whether a link can be used in the bush
	 * @param uv the link to determine candidacy
	 * @return true if the link can be used in the bush
	 */
	public boolean isValidLink(Link uv) {
		//If the link is a centroid connector
		return uv instanceof CentroidConnector? 
				//that doesn't lead from the origin and
				uv.getTail().equals(origin.getNode())? true :
					//leads from a different centroid instead
					(uv.getHead().isCentroid() && !uv.getTail().isCentroid())? true:
						//then we can't use the link in the bush
						false
				//Otherwise, we can
				: true;
	}

	/**Relax the longest path while doing longest path topological search
	 * @param l the link to examine as a candidate for the longest path
	 * @param cache a cache of longest path costs
	 * @throws UnreachableException if a node can´t be reached
	 */
	private void longRelax(Link l, Map<Node, Double> cache) throws UnreachableException {
		Double Uicij = l.getPrice(vot, c) + getCachedU(l.getTail(), cache);
		Node head = l.getHead();
		if (getqLong(head) == null || Uicij > getCachedU(head, cache)) {
			BackVector back = q.get(head);
			if (back instanceof BushMerge) ((BushMerge) back).setLongLink(l);
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

	/**
	 * @param l link to be removed
	 * @return whether the link was removed successfully
	 */
	private boolean remove(Link l) {
		BackVector b = q.get(l.getHead());
		// If there was a merge present at the head node, attempt to remove the link from it
		if (b instanceof BushMerge) {
			if (((BushMerge) b).remove(l)){ //If there is only one link left, replace BushMerge with Link
				q.put(l.getHead(),((BushMerge) b).iterator().next());
			} 
		}
		// If something unusual happened, throw a Runtime exception
		throw new RuntimeException("A link was removed that wasn't in the bush");
	}

	/**
	 * Remove unused links that aren't needed for connectivity
	 */
	void prune() {
		// TODO optimize for new bush structure
		//For each link in the bush
		for (Link l : getLinks()) {
			//Determine if there is less flow than the machine epsilon
			if (getFlow(l) < Math.ulp(getFlow(l))*2) {
				// Check to see if this link is needed for connectivity, deactivate link in bush
				deactivate(l);
			}

		}
	}

	/* (non-Javadoc)
	 * @see edu.utexas.wrap.assignment.AssignmentContainer#getFlow(edu.utexas.wrap.net.Link)
	 */
	public Double getFlow(Link l) {
		//Get the reverse topological ordering and a place to store node flows
		DemandMap flow = demand.clone();
		Iterator<Node> iter = getTopologicalOrder().descendingIterator();
		
		//For each node in reverse topological order
		for (Node n = iter.next(); iter.hasNext(); n = iter.next()) {
			BackVector back = q.get(n);
			Float downstream = flow.get(n);
			//Get the node flow and the backvector that feeds it into the node
			
			//If there is only one backvector,all node flow must pass through it
			if (back instanceof Link) {
				//So if this link is the target link, just return the node flow
				if (l.getHead().equals(n)) return (double) downstream;
				//If this link isn't the backvector for its node, return 0
				else if (n.equals(l.getHead())) return 0.0;
				
				//Otherwise, add the node flow onto the upstream node flow
				Node tail = ((Link) back).getTail();
				flow.put(tail, flow.get(tail) + downstream);
			}
			
			//If there is more than one link flowing into the node
			else if (back instanceof BushMerge) {
				for (Link bv : (BushMerge) back) {
					//Calculate the share of the node flow that uses each link
					Double share = ((BushMerge) back).getSplit(bv)*downstream;
					
					//If we've determined the share of the correct link, return it
					if (bv.equals(l)) return share;
					
					//Otherwise, add the node flow onto the upstream node flow
					Node tail = bv.getTail();
					flow.put(tail, flow.get(tail) + share.floatValue());
				}
				//If the link flows into this node but isn't in the bush, return 0
				if (l.getHead().equals(n)) return 0.0;
			}
			
			//If we've reached a dead end in the topological ordering, throw an exception
			else if (back == null && !n.equals(origin.getNode())) {
				throw new RuntimeException("Missing backvector for "+n.toString());
			}
			
		}
		//If we've examined the whole bush and didn't find the link or its head node, return 0.0;
		return 0.0;
	}

	/** Relax the shortest path while doing topological shortest path search
	 * @param l the link to examine as  a candidate for shortest path
	 * @param cache a cache of shortest path costs
	 * @throws UnreachableException if a node can't be reached
	 */
	private void shortRelax(Link l, Map<Node, Double> cache) throws UnreachableException {
		Double Licij = l.getPrice(vot, c) + getCachedL(l.getTail(), cache);

		Node head = l.getHead();
		if (getqShort(head) == null || Licij < getCachedL(head, cache)) {
			BackVector back = q.get(head);
			if (back instanceof BushMerge) ((BushMerge) back).setShortLink(l);
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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "ORIG=" + origin.getNode().getID() + "\tVOT=" + vot + "\tCLASS=" + c;
	}

	/* (non-Javadoc)
	 * @see edu.utexas.wrap.assignment.AssignmentContainer#getLinks()
	 */
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
