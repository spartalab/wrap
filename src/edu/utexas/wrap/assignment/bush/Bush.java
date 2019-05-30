package edu.utexas.wrap.assignment.bush;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import edu.utexas.wrap.assignment.AssignmentContainer;
import edu.utexas.wrap.assignment.Path;
import edu.utexas.wrap.demand.AutoDemandMap;
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

	// Bush attributes
	private final BushOrigin origin;
	private final Float vot;
	private final Mode c;

	//Underlying problem characteristics
	private final Graph wholeNet;
	private final DemandMap demand;

	// Back vector map (i.e. the bush structure)
	private Map<Node, BackVector> q;

	//Topological order can be cached for expediency
	private LinkedList<Node> cachedTopoOrder;

	/** Default constructor
	 * @param o the root of the bush
	 * @param g the graph underlying the bush
	 * @param vot the value of time
	 * @param destDemand the demand to be carried on the bush
	 * @param c the mode of travel for the bush
	 */
	public Bush(BushOrigin o, Graph g, Float vot, AutoDemandMap destDemand, Mode c) {
		origin = o;
		this.vot = vot;
		this.c = c;
		wholeNet = g;
		demand = destDemand;
	}

	/**
	 * Obtain the initial structure (shortest path tree) from the BushOrigin
	 */
	void getOriginStructure() {
		q = new Object2ObjectOpenHashMap<Node,BackVector>(origin.getInitMap(wholeNet));
	}

	/** Add a link to the bush
	 * @param l the link to be added
	 * @return whether the link was successfully added to the bush
	 */
	private boolean add(Link l) {
		Boolean ret = false;
		Node head = l.getHead();
		BackVector prior = q.get(head);
		// If there was no backvector (should only happen during bush initialization), add link
		if (prior == null) {
			q.put(head, l);
			ret = true;
		}
		// If the link is already the sole backvector, do nothing
		else if (prior.equals(l)) ret = false;
		// If the head node already has a merge backvector, delegate 
		else if (prior instanceof BushMerge) {
			ret = ((BushMerge) prior).add(l);
		}
		// Else, we need to create a new merge backvector to replace the current back link
		else if (prior instanceof Link) { // If we're here, the back link needs to be replaced by a merge
			BushMerge nuevo = new BushMerge(this, (Link) prior, l);
			q.put(head, nuevo);
			ret = true;
		}
		if (ret) cachedTopoOrder = null;
		return ret;
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
					back != null && back.equals(i);
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
		Set<Node> lNodes = new ObjectOpenHashSet<Node>();
		Set<Node> uNodes = new ObjectOpenHashSet<Node>();
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
	 */
	void dumpFlow() {
		Map<Node,Double> d = demand.doubleClone();
		for (Node node : getTopologicalOrder()) {

			Double x = d.getOrDefault(node, 0.0);
			if (x <= 0.0) continue;
			dumpFlow(node,x);
		}
	}
	
	private void dumpFlow(Node node, Double x) {
		if (node.equals(origin.getNode())) return;
		BackVector bv = q.get(node);
		if (bv instanceof Link) {
			Link l = (Link) bv;
			l.changeFlow(x);
			dumpFlow(l.getTail(),x);
		}
		else if (bv instanceof BushMerge) {
			BushMerge bm = (BushMerge) bv;
			for (Link l : bm) {
				Float split = bm.getSplit(l);
				l.changeFlow(x*split);
				dumpFlow(l.getTail(),x*split);
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
		cachedTopoOrder = to;
		return to;
	}

	/**Recursively calculate the shortest path cost to a node using a cache
	 * @param n the node to calculate the cost of the shortest path to
	 * @param cache the cache used to avoid recomputing
	 * @return the cost of the shortest path to the given node
	 * @throws UnreachableException if a node can't be reached
	 */
	public Double getCachedL(Node n, Map<Node, Double> cache) throws UnreachableException {
		Link back = getqShort(n); //Next link on the shortest path
		if (n.equals(origin.getNode())) //Return 0 at the origin
			return 0.0;
		else if (back == null)	//Something went wrong - can't find a backvector
			throw new UnreachableException(n, this);
		else if (cache.containsKey(n))	//If the value's been calculated before,
			return cache.get(n);	//return the cached value
		else {	//Calculate the value recursively, adding to the prior value
			Double newL = getCachedL(back.getTail(), cache) + back.getPrice(vot, c);
			cache.put(n, newL);	//Store this value in the cache
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
		Link back = getqLong(n);	//The next link in the longest path
		if (n.equals(origin.getNode()))	//Return 0 at the origin
			return 0.0;
		else if (back == null)	//Something went wrong - can't find the longest path
			throw new UnreachableException(n, this);
		else if (cache.containsKey(n))	//If this value was already calculated,
			return cache.get(n);	//return the cached value
		else {	//calculate from scratch, adding to the prior link's value
			Double newU = getCachedU(back.getTail(), cache) + back.getPrice(vot, c);
			cache.put(n, newU);	//store this value in the cache
			return newU;
		}
	}

	/* (non-Javadoc)
	 * @see edu.utexas.wrap.assignment.AssignmentContainer#getDemand(edu.utexas.wrap.net.Node)
	 */
	public Float getDemand(Node node) {
		return demand.get(node);
	}
	
	public DemandMap getDemandMap(){
		return demand;
	}
	
	/**Recursively calculate the shortest path cost to a node
	 * @param n the node to calculate the cost of the shortest path to
	 * @return the cost of the shortest path to the given node
	 * @throws UnreachableException if a node can't be reached
	 */
	@Deprecated
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
		if (end.equals(start))	//The path from a node to itself is empty
			return p;
		Link curLink = getqLong(end);	//Start from the prior link on the longest path
		//Until you run out of road,
		while (curLink != null && !curLink.getHead().equals(start)) {
			//Keep adding to the path the prior longest cost path link
			p.addFirst(curLink);
			curLink = getqLong(curLink.getTail());
		}
		//Check to ensure we made it to the correct node
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

	/**Get the bush's origin
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
		return qq instanceof Link? (Link) qq :	//If the BackVector is a Link, it's the only candidate
			qq instanceof BushMerge? ((BushMerge) qq).getLongLink() :	//Else delegate to the BushMerge
				null;	//This shouldn't happen
	}

	/**Get the shortest path backvector from a given node
	 * @param n the node whose shortest path backvector should be returned
	 * @return the last link in the shortest path to the given node
	 */
	public Link getqShort(Node n) {
		BackVector qq = q.get(n);
		return qq instanceof Link? (Link) qq :	//If the BackVector is a Link, it's the only candidate
			qq instanceof BushMerge ? ((BushMerge) qq).getShortLink() :	//Otherwise, delegate to the BushMerge
				null;	//This shouldn't happen
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
		if (!(q.get(terminus) instanceof BushMerge) || longLink == null || longLink.equals(shortLink))
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
		if (end.equals(start))	//The path from a node to itself is empty
			return p;
		Link curLink = getqShort(end);	//Start with the previous shortest cost path link
		while (curLink != null && !curLink.getHead().equals(start)) {
			//Keep adding the next shortest cost path link until you run out of road
			p.addFirst(curLink);
			curLink = getqShort(curLink.getTail());
		}
		//Check to ensure we made it to the start node
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
	@Deprecated
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
		//Calculate the cost of adding the link to the longest path
		Double Uicij = l.getPrice(vot, c) + getCachedU(l.getTail(), cache);
		Node head = l.getHead();
		//If the longest path doesn't exist, is already through the proposed link, or the cost is longer,
		if (getqLong(head) == null || getqLong(head).equals(l) || Uicij > getCachedU(head, cache)) {
			//Update the BushMerge if need be
			BackVector back = q.get(head);
			if (back instanceof BushMerge) ((BushMerge) back).setLongLink(l);
			//Store the cost in the cache
			cache.put(head, Uicij);
		}
	}

	/**
	 * Calculate longest paths in bush (DAG) using topological search
	 * 
	 * Leverage the presence of a topological order to decrease search time for
	 * longest paths calculation
	 * @param longestUsed whether to relax only links that are in use
	 */
	public Map<Node, Double> longTopoSearch(boolean longestUsed) {
		List<Node> to = getTopologicalOrder();
		Map<Node, Double> cache = new Object2DoubleOpenHashMap<Node>(wholeNet.numNodes());

		//In topological order,
		for (Node d : to) {
			try {
				//Try to relax the backvector (all links in the BushMerge, if applicable)
				BackVector bv = q.get(d);
				if (bv instanceof Link) longRelax((Link) bv, cache);
				else if (bv instanceof BushMerge) {
					BushMerge bm = (BushMerge) bv;
					for (Link l : bm) {
						if (!longestUsed || bm.getSplit(l) > 0.0) longRelax(l,cache);
					}
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
				return true;
			} 
			return false;
		}
		// If something unusual happened, throw a Runtime exception
		throw new RuntimeException("A link was removed that wasn't in the bush");
	}

	/**
	 * Remove unused links that aren't needed for connectivity
	 */
	void prune() {
		for (BackVector v : q.values()) {
			if (v instanceof BushMerge) {	//For every BushMerge in the bush
				BushMerge bm = new BushMerge((BushMerge) v);
				for (Link l : bm) {	//See if the split is approximately 0
					if (bm.getSplit(l) <= Math.ulp(bm.getSplit(l))*2) {
						//If so, try to remove the Link
						if (remove(l)) cachedTopoOrder = null;
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see edu.utexas.wrap.assignment.AssignmentContainer#getFlow(edu.utexas.wrap.net.Link)
	 */
	public Double getFlow(Link l) {
		//Get the reverse topological ordering and a place to store node flows
		Map<Node,Double> flow = demand.doubleClone();
		Iterator<Node> iter = getTopologicalOrder().descendingIterator();
		
		//For each node in reverse topological order
		for (Node n = iter.next(); iter.hasNext(); n = iter.next()) {
			BackVector back = q.get(n);
			Double downstream = flow.getOrDefault(n,0.0);
			//Get the node flow and the backvector that feeds it into the node
			
			//If there is only one backvector,all node flow must pass through it
			if (back instanceof Link) {
				//So if this link is the target link, just return the node flow
				if (l.getHead().equals(n)) return (double) downstream;
				//If this link isn't the backvector for its node, return 0
				else if (n.equals(l.getHead())) return 0.0;
				
				//Otherwise, add the node flow onto the upstream node flow
				Node tail = ((Link) back).getTail();
				Double newf = flow.getOrDefault(tail,0.0)+downstream;
				if (newf.isNaN()) {	//NaN check
					throw new RuntimeException();
				}
				flow.put(tail, newf);
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
					Double newf = flow.getOrDefault(tail,0.0)+share.doubleValue();
					if (newf.isNaN()) {	//NaN check
						throw new RuntimeException();
					}
					flow.put(tail, flow.getOrDefault(tail,0.0) + share.doubleValue());
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
	
	/** Get all Bush flows
	 * @return a Map from a Link to the amount of flow from this Bush on the Link
	 */
	@Override
	public Map<Link, Double> getFlows(){
		//Get the reverse topological ordering and a place to store node flows
//		Map<Node,Double> nodeFlow = new HashMap<Node,Double>();
//		for (Node d : demand.getNodes()) nodeFlow.put(d, demand.get(d).doubleValue());
		
		Map<Node,Double> nodeFlow = demand.doubleClone();
		Iterator<Node> iter = getTopologicalOrder().descendingIterator();
		Map<Link,Double> ret = new Object2DoubleOpenHashMap<Link>();

		//For each node in reverse topological order
		for (Node n = iter.next(); iter.hasNext(); n = iter.next()) {
			//Get the node flow and the backvector that feeds it into the node
			BackVector back = q.get(n);
			Double downstream = nodeFlow.getOrDefault(n,0.0);

			//If there is only one backvector,all node flow must pass through it
			if (back instanceof Link) {						
				//Add the node flow onto the upstream node flow
				Link b = (Link) back;
				Node tail = b.getTail();
				nodeFlow.put(tail, nodeFlow.getOrDefault(tail,0.0) + downstream);
				ret.put(b, (double) downstream);
			}

			//If there is more than one link flowing into the node
			else if (back instanceof BushMerge) {
				for (Link bv : (BushMerge) back) {
					//Calculate the share of the node flow that uses each link
					Double share = ((BushMerge) back).getSplit(bv)*downstream;

					//Add the node flow onto the upstream node flow
					Node tail = bv.getTail();
					nodeFlow.put(tail, nodeFlow.getOrDefault(tail,0.0) + share.doubleValue());
					ret.put(bv, share);
				}
			}

			//If we've reached a dead end in the topological ordering, throw an exception
			else if (back == null && !n.equals(origin.getNode())) {
				throw new RuntimeException("Missing backvector for "+n.toString());
			}
			
		}
		return ret;
	}

	/** Relax the shortest path while doing topological shortest path search
	 * @param l the link to examine as  a candidate for shortest path
	 * @param cache a cache of shortest path costs
	 * @throws UnreachableException if a node can't be reached
	 */
	private void shortRelax(Link l, Map<Node, Double> cache) throws UnreachableException {
		//Calculate the cost of adding this link to the shortest path
		Double Licij = l.getPrice(vot, c) + getCachedL(l.getTail(), cache);
		Node head = l.getHead();
		
		//If the shortest path doesn't exist, already flows through the link, or this has a lower cost,
		if (getqShort(head) == null || getqShort(head).equals(l) || Licij < getCachedL(head, cache)) {
			//Update the BushMerge, if applicable
			BackVector back = q.get(head);
			if (back instanceof BushMerge) ((BushMerge) back).setShortLink(l);
			//Store this cost in the cache
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
		Map<Node, Double> cache = new Object2DoubleOpenHashMap<Node>(wholeNet.numNodes());

		//In topological order,
		for (Node d : to) {
			try {
				for (Link l : wholeNet.inLinks(d)) {
					//Try to relax all incoming links
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

	/**Improve the bush by removing unused links and adding shortcut links
	 * @return whether the bush structure was modified
	 */
	public boolean improve() {
		prune();	//Remove unused links

		boolean modified = false;
		Set<Link> usedLinks = getLinks();
		Set<Link> unusedLinks = new HashSet<Link>(wholeNet.getLinks());
		unusedLinks.removeAll(usedLinks);
		
		//Calculate the longest path costs
		Map<Node, Double> cache = longTopoSearch(true);
		Set<Link> tba = new HashSet<Link>();	//Set of links to be added
		for (Link l : unusedLinks) {
			// If link is active, do nothing (removing flow should mark as inactive)
			//Could potentially delete both incoming links to a node
			if (!l.allowsClass(getVehicleClass()) || !isValidLink(l)) continue;
			try {
				// Else if Ui + tij < Uj
				Double tailU = getCachedU(l.getTail(), cache);
				Double headU = getCachedU(l.getHead(), cache);
				Double linkVal = l.getPrice(getVOT(), getVehicleClass());
				
				if (tailU + linkVal < headU) {
					tba.add(l);	//Mark the link as one which should be added
					modified = true;
				}
			} catch (UnreachableException e) {
				if (e.demand > 0) e.printStackTrace();
				continue;
			}

		}
		//Add all marked links to the Bush
		for (Link l : tba) add(l);
		return modified;
	}
	
	/* (non-Javadoc)
	 * @see edu.utexas.wrap.assignment.AssignmentContainer#getLinks()
	 */
	@Override
	public Set<Link> getLinks() {
		Set<Link> ret = new HashSet<Link>();
		for (BackVector b : q.values()) {
			//For every backvector, add its link(s) to the return set
			if (b instanceof Link) ret.add((Link) b);
			else if (b instanceof BushMerge) 
				ret.addAll((BushMerge) b);
		}
		return ret;

	}

	/**Calculate the most flow that can be shifted at this node
	 * @param cur the node where flow should be balanced
	 * @param bushFlows the current flows on the bush
	 * @return the maximum flow that can be shifted from the longest to the shortest path
	 */
	public Double getMaxDelta(Node cur, Map<Link, Double> bushFlows) {
		BackVector bv = q.get(cur);
		return bv instanceof BushMerge ? //If the backvector is a BushMerge,
				((BushMerge) bv).getMaxDelta(bushFlows) //Delegate finding the maximum delta
				: 0.0; //Otherwise the maximum delta is zero since there's only one path here
	}

	/**Update the BushMerges' splits based on current Bush flows
	 * @param flows the current Bush flows on all Links
	 */
	public void updateSplits(Map<Link, Double> flows) {
		for (BackVector bv : q.values()) {
			if (bv instanceof BushMerge) {	//For each BushMerge in the Bush
				BushMerge bm = (BushMerge) bv;
				double total = 0.0;	//Calculate the total demand through this node
				for (Link l : bm) {
					total += flows.get(l);
				}
				//If there is no total, leave the splits alone
				if (total > 0) for (Link l : bm) {
					//Otherwise, set them proportional to total demand share
					bm.setSplit(l, (float) (flows.get(l)/total));
				}
			}
		}
	}
	
	/**This method writes the structure of the bush to a given print stream
	 * @param out the print stream to which the structure will be written
	 * @throws IOException 
	 */
	public void toFile(OutputStream out) throws IOException {
		int size = Integer.BYTES*2+Float.BYTES;
		for (Node n : getTopologicalOrder()) {
			BackVector qn = q.get(n);
			
			if (qn instanceof Link) {
//				out.println(n.getID()+","+((Link) qn).hashCode()+",1.0");
				byte[] b = ByteBuffer.allocate(size)
						.putInt(n.getID())
						.putInt(((Link) qn).hashCode())
						.putFloat(1.0F)
						.array();
				out.write(b);
			}
			
			else if (qn instanceof BushMerge) {
				BushMerge qm = (BushMerge) qn;
				for (Link l : qm) {
					byte[] b = ByteBuffer.allocate(size)
							.putInt(n.getID())
							.putInt(l.hashCode())
							.putFloat(qm.getSplit(l))
							.array();
					out.write(b);
					}
			}
			out.flush();
		}
	}
	
	/**Attempt to read the bush structure from a file, rather than building a
	 * new structure using Dijkstra's algorithm
	 * @param in
	 * @throws IOException 
	 */
	public void fromFile(BufferedInputStream in) throws IOException {
		q = new Object2ObjectOpenHashMap<Node,BackVector>();
		byte[] b = new byte[Integer.BYTES*2+Float.BYTES];
		
		//For each link in the bush
		while (in.available() >= Integer.BYTES*2+Float.BYTES) {
			//File IO, formatting
			in.read(b);
			ByteBuffer bb = ByteBuffer.wrap(b);
			Integer nid = bb.getInt();
			Integer bvhc = bb.getInt();
			Float split = bb.getFloat();
			Node n = wholeNet.getNode(nid);

			//Find the appropriate link instance
			Link bv = null;
			for (Link l : wholeNet.inLinks(n)) {
				if (l.hashCode() == bvhc) {
					bv = l;
					break;
				}
			}
			//If it can't be found, throw an error
			if (bv == null) throw new RuntimeException("Unknown Link");
			
			//If this is the first link read which leads to this head node
			if (!q.containsKey(n) ) {
				//Check to see if this holds all flow through this node
				if (split == 1.0) q.put(n, bv);
				//create a merge if it doesn't
				else {
					BushMerge qm = new BushMerge(this);
					qm.add(bv);
					qm.setSplit(bv, split);
					q.put(n, qm);
				}
			}
			else {
				BackVector qb = q.get(n);
				//If we already constructed the merge
				if (qb instanceof BushMerge) {
					//add the link
					BushMerge qm = (BushMerge) qb;
					qm.add(bv);
					qm.setSplit(bv, split);
				}
				else if (qb instanceof Link) {
					//otherwise construct a new one
					BushMerge qm = new BushMerge(this, (Link) qb, bv);
					qm.setSplit(bv, split);
					q.put(n, qm);
				}
			}
		}
	}

	/**This method searches for a bush structure file in the location
	 * given by the relative path
	 * "./{MD5 hash of input graph file}/{origin ID}/{Mode}-{VOT}.bush"
	 * @throws IOException if the defined path is not found or is corrupt
	 */
	public void loadStructureFile() throws IOException {
		//Convert the graph's MD5 hash to a hex string
		StringBuilder sb = new StringBuilder();
		for (byte b : wholeNet.getMD5()) {
			sb.append(String.format("%02X", b));
		}
		
		//find the file at the predefined relative path
		File file = new File(sb+"/"+
		getOrigin().getNode().getID()+"/"+
				getVehicleClass()+"-"+getVOT()+".bush");
		
		//Read the file in
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
		fromFile(in);
		in.close();
	}
}
