package edu.utexas.wrap.assignment.bush;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
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
import edu.utexas.wrap.util.UnreachableException;

/** An instance of a {@link edu.utexas.wrap.assignment.AssignmentContainer}
 * that is used for bush-based assignment methods.
 * @author William
 *
 */
public class Bush implements AssignmentContainer {

	public static boolean cachingAllowed = true;
	// Bush attributes
	private final BushOrigin origin;
	private final Float vot;
	private final Mode c;

	//Underlying problem characteristics
	private final Graph network;
	private final DemandMap demand;

	// Back vector map (i.e. the bush structure)
	private BackVector[] q;

	//Topological order can be cached for expediency
	private Node[] cachedTopoOrder;
	
	private Semaphore writing;

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
		network = g;
		demand = destDemand;
		writing = new Semaphore(1);
	}

	/**
	 * Obtain the initial structure (shortest path tree) from the BushOrigin
	 */
	void getOriginStructure() {
		q = origin.getInitMap(network).clone();
	}

	/** Add a link to the bush
	 * @param l the link to be added
	 * @return whether the link was successfully added to the bush
	 */
	private boolean add(Link l) {
		Boolean ret = false;
		Node head = l.getHead();
		BackVector prior = getBackVector(head);
		// If there was no backvector (should only happen during bush initialization), add link
		if (prior == null) {
			setBackVector(head, l);
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
			setBackVector(head, nuevo);
			ret = true;
		}
		if (ret) cachedTopoOrder = null;
		return ret;
	}

	/**Set the backvector of a given node
	 * @param head the node where the BackVector leads
	 * @param backvector the BackVector associated with the node
	 */
	void setBackVector(Node head, BackVector backvector) {
		q[network.getOrder(head)] = backvector;
	}

	/**Determine whether a link is active in the bush
	 * @param i the link which should be checked
	 * @return whether the given link is in the bush structure
	 */
	private boolean contains(Link i) {
		BackVector back = getBackVector(i.getHead());
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
		if (start.equals(origin.getNode()) || !(getBackVector(start) instanceof BushMerge))
			return start;

		//Store the nodes seen on the paths in reverse order
		List<Node> lNodes = new ObjectArrayList<Node>();
		List<Node> uNodes = new ObjectArrayList<Node>();

		//Iterate backwards through the shortest and longest paths until one runs out of links
		Link uLink = getqLong(start);
		Link lLink = getqShort(start);
		while (lLink != null && uLink != null) {

			Node lNode = lLink.getTail();
			Node uNode = uLink.getTail();

			//If the next node in both is the same, return that node
			if (lNode.equals(uNode)) return lNode;
			//If the shortest path just got to a node that was already seen in the longest path, return it
			else if (uNodes.contains(lNode)) return lNode;
			//If the longest path just got to a node that was already seen in the shortest path, return it
			else if (lNodes.contains(uNode)) return uNode;
			//Otherwise, add both to the lists of seen nodes
			else {
				lNodes.add(lLink.getTail());
				uNodes.add(uLink.getTail());
			}
			
			lLink = getqShort(lNode);
			uLink = getqLong(uNode);
		}
		//If there are still more links to examine on the longest path, do so
		while (uLink != null) {
			Node uNode = uLink.getTail();
			//If the longest path just got to a node that was already seen in the shortest path, return it
			if (lNodes.contains(uNode)) return uNode;
			uLink = getqLong(uNode);
		}
		//If there are still more links to examine on the shortest path, do so
		while (lLink != null) {
			Node lNode = lLink.getTail();
			//If the shortest path just got to a node that was already seen in the longest path, return it
			if (uNodes.contains(lNode))	return lNode;
			lLink = getqShort(lNode);
		}
		//Something went wrong - the two paths never intersected
		throw new RuntimeException("No diverge node found");
	}

	/**
	 * Initialize demand flow on shortest paths Add each destination's demand to the
	 * shortest path to that destination
	 */
	void dumpFlow() {
		for (BackVector bv : q) {
			if (bv == null) continue;
			Node node = bv.getHead();
			Float x = demand.getOrDefault(node, 0.0F);
			if (x <= 0.0) continue;
			dumpFlow(node,x);	//recursively push flow onto the bush
		}
	}
	
	/**Recursive helper method to load demand onto links on the bush
	 * @param node the node on whose backvector demand should be loaded
	 * @param x the amount of demand passing through that node
	 */
	private void dumpFlow(Node node, Float x) {
		//If we've reached the end of the bush or there's no demand, stop
		if (node.equals(origin.getNode()) || x == 0.0) return;
		
		//For each link leading into the current node
		BackVector bv = getBackVector(node);
		if (bv instanceof Link) { //If there's only one such link
			//modify it and move to the next level
			Link l = (Link) bv;
			l.changeFlow(x.doubleValue());
			dumpFlow(l.getTail(),x);
		}
		else if (bv instanceof BushMerge) {
			//Otherwise recursively load onto all links in the backvector
			BushMerge bm = (BushMerge) bv;
			for (Link l : bm) {
				Float split = bm.getSplit(l);
				if (split == 0.0f) continue;
				l.changeFlow(x.doubleValue()*split);
				dumpFlow(l.getTail(),x*split);
			}
		}
	}

	/**Generate a topological ordering from scratch for this bush
	 * @return Nodes in topological order
	 */
	private Node[] generateTopoOrder(boolean toCache) {
		// Start with a set of all bush edges
		Set<Link> currentLinks = getLinks();

		Node[] to = new Node[network.numNodes()];
		LinkedList<Node> S = new LinkedList<Node>();
		// "start nodes"
		S.add(origin.getNode());
		Node n;
		int pos = 0;

		while (!S.isEmpty()) {
			n = S.pop();// remove node from S
			to[pos] = n; // append node to L
			pos++;
			
			// for each active edge out of this node
			for (Link l : network.outLinks(n)) {
				if (currentLinks.contains(l)) {

					// remove the links from the set
					currentLinks.remove(l);
					// the node on the other end
					Node m = l.getHead();

					// see if this node has no other incoming active links
					boolean mHasIncoming = false;
					for (Link e : network.inLinks(m)) {
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
		if (Bush.cachingAllowed || toCache) cachedTopoOrder = to;
		return to;
	}

	/**Recursively calculate the shortest path cost to a node using a cache
	 * @param n the node to calculate the cost of the shortest path to
	 * @param cache the cache used to avoid recomputing
	 * @return the cost of the shortest path to the given node
	 * @throws UnreachableException if a node can't be reached
	 */
	public Double getCachedL(Node n, Double[] cache) throws UnreachableException {
		Link back = getqShort(n); //Next link on the shortest path
		if (n.equals(origin.getNode())) //Return 0 at the origin
			return 0.0;
		else if (back == null)	//Something went wrong - can't find a backvector
			throw new UnreachableException(n, this);
		else if (cache[network.getOrder(n)] != null)	//If the value's been calculated before,
			return cache[network.getOrder(n)];	//return the cached value
		else {	//Calculate the value recursively, adding to the prior value
			Double newL = getCachedL(back.getTail(), cache) + back.getPrice(vot, c);
			cache[network.getOrder(n)] = newL;	//Store this value in the cache
			return newL;
		}
	}

	/**Recursively calculate the longest path cost to a node using a cache
	 * @param n the node to calculate the cost of the longest path to
	 * @param cache the cache used to avoid recomputing
	 * @return the cost of the longest path to the given node
	 * @throws UnreachableException if a node can't be reached
	 */
	public Double getCachedU(Node n, Double[] cache) throws UnreachableException {
		Link back = getqLong(n);	//The next link in the longest path
		if (n.equals(origin.getNode()))	//Return 0 at the origin
			return 0.0;
		else if (back == null) {	//Something went wrong - can't find the longest path
			if (getDemand(n) > 0.0) throw new UnreachableException(n, this);
			else {
				cache[network.getOrder(n)] = Double.MAX_VALUE;
				return Double.MAX_VALUE;
			}
		}
		else if (cache[network.getOrder(n)] != null)	//If this value was already calculated,
			return cache[network.getOrder(n)];	//return the cached value
		else {	//calculate from scratch, adding to the prior link's value
			Double newU = getCachedU(back.getTail(), cache) + back.getPrice(vot, c);
			cache[network.getOrder(n)] = newU;	//store this value in the cache
			return newU;
		}
	}

	/* (non-Javadoc)
	 * @see edu.utexas.wrap.assignment.AssignmentContainer#getDemand(edu.utexas.wrap.net.Node)
	 */
	public Float getDemand(Node node) {
		return demand.get(node);
	}
	
	/**
	 * @return the demand map associated with this bush
	 */
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
		return network.getNodes();
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
		BackVector qq = getBackVector(n);
		return qq instanceof Link? (Link) qq :	//If the BackVector is a Link, it's the only candidate
			qq instanceof BushMerge? ((BushMerge) qq).getLongLink() :	//Else delegate to the BushMerge
				null;	//This shouldn't happen
	}

	/**Get the shortest path backvector from a given node
	 * @param n the node whose shortest path backvector should be returned
	 * @return the last link in the shortest path to the given node
	 */
	public Link getqShort(Node n) {
		BackVector qq = getBackVector(n);
		return qq instanceof Link? (Link) qq :	//If the BackVector is a Link, it's the only candidate
			qq instanceof BushMerge ? ((BushMerge) qq).getShortLink() :	//Otherwise, delegate to the BushMerge
				null;	//This shouldn't happen
	}

	/**Assemble the alternate segment pair of longest and shortest paths emanating from the terminus
	 * @param terminus the node whose longest-shortest ASP should be calculated
	 * @param bushFlows 
	 * @return the alternate segment pair consisting of the shortest and longest paths to the terminus
	 */
	public AlternateSegmentPair getShortLongASP(Node terminus, Map<Link, Double> bushFlows) {
		//Iterate through longest paths until reaching a node in shortest path

		//Reiterate through shortest path to build path up to divergence node
		//The two paths constitute a Pair of Alternate Segments
		Link shortLink = getqShort(terminus);
		Link longLink = getqLong(terminus);

		// If there is no divergence node, move on to the next topological node
		if (!(getBackVector(terminus) instanceof BushMerge) || longLink == null || longLink.equals(shortLink))
			return null;

		// Else calculate divergence node
		Node diverge = divergeNode(terminus);

		//Trace back through the longest path
		Node cur = terminus;
		Link ll = getqLong(cur);
		Double max = null;
		int lpl = 0;
		do {
			//Keep track of the number of links until the diverge
			lpl++;
			//Keep track of the maximum bush flow that can be removed
			if (max == null) max = bushFlows.get(ll);
			else max = Math.min(max,bushFlows.get(ll));
			cur = ll.getTail();
			ll = getqLong(cur);
		} while (cur != diverge);
		
		
		//Trace back through the shortest path
		cur = terminus;
		ll = getqShort(cur);
		int spl = 0;
		do {
			//Count how many links until the diverge
			spl++;
			cur = ll.getTail();
			ll = getqShort(cur);
		} while (cur != diverge);
		
		return new AlternateSegmentPair(terminus, diverge, max, this, lpl, spl);
	}

	/**Get the backvector at a given node
	 * @param node where the BackVector leads
	 * @return the backvector from this bush leading to this node
	 */
	BackVector getBackVector(Node node) {
		return q[network.getOrder(node)];
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
	public Node[] getTopologicalOrder(boolean toCache) {
		return (cachedTopoOrder != null) ? cachedTopoOrder : generateTopoOrder(toCache);
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
		
		return (
				origin.getNode().getID()*a + vot.intValue()
				)*b + (c == null? 0 : c.hashCode());
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
	private void longRelax(Link l, Double[] cache) throws UnreachableException {
		//Calculate the cost of adding the link to the longest path
		Double Uicij = l.getPrice(vot, c) + getCachedU(l.getTail(), cache);
		Node head = l.getHead();
		
		BackVector back = getBackVector(head);
		//If the longest path doesn't exist, is already through the proposed link, or the cost is longer,
		if (back.getLongLink() == null || Uicij > getCachedU(head, cache)) {
			//Update the BushMerge if need be
			if (back instanceof BushMerge) ((BushMerge) back).setLongLink(l);
			//Store the cost in the cache
			cache[network.getOrder(head)] = Uicij;
		}
	}

	/**
	 * Calculate longest paths in bush (DAG) using topological search
	 * 
	 * Leverage the presence of a topological order to decrease search time for
	 * longest paths calculation
	 * @param longestUsed whether to relax only links that are in use
	 */
	public Double[] longTopoSearch(boolean longestUsed) throws UnreachableException {
		Node[] to = getTopologicalOrder(false);
		Double[] cache = new Double[network.numNodes()];

		//In topological order,
		for (Node d : to) {

			//Try to relax the backvector (all links in the BushMerge, if applicable)
			BackVector bv = getBackVector(d);
			if (bv instanceof Link) longRelax((Link) bv, cache);
			else if (bv instanceof BushMerge) {
				BushMerge bm = (BushMerge) bv;
				for (Link l : bm) {
					if (!longestUsed || bm.getSplit(l) > 0.0) longRelax(l,cache);
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
		BackVector b = getBackVector(l.getHead());
		// If there was a merge present at the head node, attempt to remove the link from it
		if (b instanceof BushMerge) {
			BushMerge bm = (BushMerge) b;
			int size = bm.size();
			if (size > 1) {
				bm.remove(l);
				if (size==2){ //If there is only one link left, replace BushMerge with Link
					setBackVector(l.getHead(),bm.iterator().next());
					return true;
				} 
			}
			return false;
		}
		else if (b instanceof Link && l.equals((Link) b)) return false;
		// If something unusual happened, throw a Runtime exception
		throw new RuntimeException("A link was removed that wasn't in the bush");
	}

	/**
	 * Remove unused links that aren't needed for connectivity
	 */
	void prune() {
		for (BackVector v : q) {
			if (v instanceof BushMerge) {	//For every BushMerge in the bush
				
				//Duplicate the link list to avoid a ConcurrentModificationException
				BushMerge bm = new BushMerge((BushMerge) v);
				
				for (Link l : bm) {	//See if the split is approximately 0
					if (bm.getSplit(l) <= Math.ulp(bm.getSplit(l))*20) {
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
	@Deprecated 
	public Double getFlow(Link l) {
		//Get the reverse topological ordering and a place to store node flows
		Map<Node,Double> flow = demand.doubleClone();
		Node[] iter = getTopologicalOrder(false);
		
		//For each node in reverse topological order
		for (int i = iter.length - 1; i >= 0; i--) {
			Node n = iter[i];
			if (n == null) continue;
			BackVector back = getBackVector(n);
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
		Map<Node,Double> nodeFlow = demand.doubleClone();
		Node[] iter = getTopologicalOrder(false);
		Map<Link,Double> ret = new Object2DoubleOpenHashMap<Link>((int) (network.numZones()*1.5),1.0f);

		//For each node in reverse topological order
		for (int i = iter.length - 1; i >= 0; i--) {
			Node n = iter[i];
			if (n == null) continue;
			
			//Get the node flow and the backvector that feeds it into the node
			BackVector back = getBackVector(n);
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
	private void shortRelax(Link l, Double[] cache) throws UnreachableException {
		//Calculate the cost of adding this link to the shortest path
		Double Licij = l.getPrice(vot, c) + getCachedL(l.getTail(), cache);
		Node head = l.getHead();
		BackVector back = getBackVector(head);
		
		//If the shortest path doesn't exist, already flows through the link, or this has a lower cost,
		if (back.getShortLink() == null || Licij < getCachedL(head, cache)) {
			//Update the BushMerge, if applicable
			if (back instanceof BushMerge) ((BushMerge) back).setShortLink(l);
			//Store this cost in the cache
			cache[network.getOrder(head)] = Licij;
		}
	}

	/**
	 * Calculate shortest paths in bush (DAG) using topological search
	 * 
	 * Leverage the presence of a topological order to decrease search time for
	 * shortest paths calculation
	 */
	public Double[] shortTopoSearch() {
		Node[] to = getTopologicalOrder(false);
		Double[] cache = new Double[network.numNodes()];

		//In topological order,
		for (Node d : to) {
			try {
				BackVector bv = getBackVector(d);
				if (bv instanceof Link) shortRelax((Link) bv, cache);
				else if (bv instanceof BushMerge) for (Link l : (BushMerge) bv) {
					//Try to relax all incoming links
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
		try {
			writing.acquire();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		prune();	//Remove unused links

		boolean modified = false;
		Set<Link> unusedLinks = new ObjectOpenHashSet<Link>(network.getLinks());
		unusedLinks.removeAll(getLinks());
		
		//Calculate the longest path costs
		Double[] cache;
		try {
			cache = longTopoSearch(true);
		} catch (UnreachableException e1) {
			q = origin.getInitMap(network);
			try{
				cache = longTopoSearch(true);
				}
			catch (UnreachableException e2) {
				throw new RuntimeException(e2);
			}
		} 
		
		Set<Link> tba = new HashSet<Link>();	//Set of links to be added
		for (Link l : unusedLinks) {
			// If link is active, do nothing (removing flow should mark as inactive)
			//Could potentially delete both incoming links to a node
			if (!l.allowsClass(getVehicleClass()) || !isValidLink(l)) continue;
			try {
				// Else if Ui + tij < Uj
				double tailU = getCachedU(l.getTail(), cache);
				double headU = getCachedU(l.getHead(), cache);
				double linkVal = l.getPrice(getVOT(), getVehicleClass());
				
				if (tailU + linkVal < headU) {
					tba.add(l);	//Mark the link as one which should be added
					modified = true;
				}
			} catch (UnreachableException e) {
				if (e.demand > 0) {
					q = origin.buildInitMap(network);
				}
				continue;
			}

		}
		//Add all marked links to the Bush
		for (Link l : tba) add(l);
		writing.release();
		return modified;
	}
	
	/* (non-Javadoc)
	 * @see edu.utexas.wrap.assignment.AssignmentContainer#getLinks()
	 */
	@Override
	public Set<Link> getLinks() {
		Set<Link> ret = new HashSet<Link>((int) (q.length*1.3));
		for (BackVector b : q) {
			//For every backvector, add its link(s) to the return set
			if (b instanceof Link) ret.add((Link) b);
			else if (b instanceof BushMerge) 
				ret.addAll(((BushMerge) b).getLinks());
		}
		return ret;

	}

	/**Update the BushMerges' splits based on current Bush flows
	 * @param flows the current Bush flows on all Links
	 */
	public void updateSplits(Map<Link, Double> flows) {
		for (BackVector bv : q) {
			if (bv instanceof BushMerge) {	//For each BushMerge in the Bush
				BushMerge bm = (BushMerge) bv;
				double total = 0.0;	//Calculate the total demand through this node
				for (Link l : bm) total += flows.get(l);
				
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
	 * @throws InterruptedException 
	 */
	public void toFile(OutputStream out) throws IOException, InterruptedException {
		writing.acquire();
		int size = Integer.BYTES*2+Float.BYTES; //Size of each link's data
		
		//For each node
		for (Node n : getTopologicalOrder(false)) {
			BackVector qn = getBackVector(n);
			//get all the links leading to the node
			//write them to a file
			if (qn instanceof Link) {
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
		writing.release();
	}
	
	/**Create a thread that writes the current bush to the default file
	 * location, namely, ./{NetworkID}/{OriginID}/{VehicleClass}-{VOT}.bush
	 * @return a thread which writes to the default file location
	 */
	public Thread toDefaultFile() {
		return new Thread() {
			public void run() {
				
				File file = new File(network.toString()+"/"+origin.getNode().getID()+"/"+getVehicleClass()+"-"+getVOT()+".bush");
				file.getParentFile().mkdirs();
				FileOutputStream out = null;
				try {
					out = new FileOutputStream(file);
					toFile(out);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				finally {
					if (out != null)
						try {
							out.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
				}
			}
		};
	}
	
	/**Attempt to read the bush structure from a file, rather than building a
	 * new structure using Dijkstra's algorithm
	 * @param in
	 * @throws IOException 
	 */
	public void fromFile(BufferedInputStream in) throws IOException {
		q = new BackVector[network.numNodes()+1];
		byte[] b = new byte[Integer.BYTES*2+Float.BYTES];
		
		//For each link in the bush
		while (in.available() >= Integer.BYTES*2+Float.BYTES) {
			//File IO, formatting
			in.read(b);
			ByteBuffer bb = ByteBuffer.wrap(b);
			Integer nid = bb.getInt();
			Integer bvhc = bb.getInt();
			Float split = bb.getFloat();
			Node n = network.getNode(nid);

			//Find the appropriate link instance
			Link bv = null;
			for (Link l : network.inLinks(n)) {
				if (l.hashCode() == bvhc) {
					bv = l;
					break;
				}
			}
			//If it can't be found, throw an error
			if (bv == null) throw new RuntimeException("Unknown Link");
			
			//If this is the first link read which leads to this head node
			if (getBackVector(n) == null) {
				//Check to see if this holds all flow through this node
				if (split == 1.0)
					setBackVector(n, bv);
				else {
					BushMerge qm = new BushMerge(this,n);
					qm.add(bv);
					qm.setSplit(bv, split);
					setBackVector(n,qm);
				}
			}
			else {
				BackVector qb = getBackVector(n);
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
					setBackVector(n,qm);
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
		for (byte b : network.getMD5()) {
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
	
	/**Delete any cached topological order to conserve space
	 * 
	 */
	public void clearCache() {
		cachedTopoOrder = null;
	}

	/**This bush is no longer being read/written
	 * 
	 */
	public void release() {
		writing.release();
	}

	/**Begin reading/writing this bush
	 * @throws InterruptedException
	 */
	public void acquire() throws InterruptedException {
		writing.acquire();
	}
}
