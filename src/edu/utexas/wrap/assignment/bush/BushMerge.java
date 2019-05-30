package edu.utexas.wrap.assignment.bush;

import java.util.Map;

import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.Node;
import edu.utexas.wrap.util.AlternateSegmentPair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

/**A way to represent the Links which merge at a given Node in a Bush,
 * namely, a set of Links with a longest and shortest path named and a
 * Map of each link's share of the total load
 * @author William
 *
 */
public class BushMerge extends ObjectOpenHashSet<Link> implements BackVector{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Link shortLink;
	private Link longLink;
	private AlternateSegmentPair asp;
	private Map<Link, Float> split;
	private final Bush bush;
	
	/** Create a BushMerge by adding a shortcut link to a node
	 * @param b the bush upon whose structure the merge depends
	 * @param u the pre-existing back-vector that is short-circuited
	 * @param l the shortcut link providing a shorter path to the node
	 */
	public BushMerge(Bush b, Link u, Link l) {
		super(4);
		bush = b;

		add(l);
		add(u);

		split = new Object2ObjectOpenHashMap<Link, Float>();
		split.put(u, 1.0F);
	}
	
	/**Duplication constructor
	 * @param bm	the BushMerge to be copied
	 */
	public BushMerge(BushMerge bm) {
		super(bm);
		bush = bm.bush;
		longLink = bm.longLink;
		shortLink = bm.shortLink;
		asp = bm.asp;
		split = new Object2ObjectOpenHashMap<Link,Float>(bm.split);
	}
	
	/**Constructor for empty merge
	 * @param b
	 */
	protected BushMerge(Bush b) {
		super(4);
		bush = b;
		split = new Object2ObjectOpenHashMap<Link,Float>();
	}
	
	/**
	 * @return the shortest cost path Link
	 */
	public Link getShortLink() {
		return shortLink;
	}
	
	/**
	 * @return the longest cost path Link
	 */
	public Link getLongLink() {
		return longLink;
	}
	
	/**
	 * @return the shortest and longest path AlternateSegmentPair merging here
	 */
	public AlternateSegmentPair getShortLongASP() {
		if (asp == null) asp = bush.getShortLongASP(shortLink.getHead());
		return asp;
	}
	
	/**Set the shortest path cost link
	 * @param l	the new shortest path Link to here
	 */
	protected void setShortLink(Link l) {
		shortLink = l;
	}
	
	/**Set the longest path cost link
	 * @param l the new longest path Link to here
	 */
	protected void setLongLink(Link l) {
		longLink = l;
	}
	
	/* (non-Javadoc)
	 * @see java.util.AbstractCollection#toString()
	 */
	public String toString() {
		return "Merge at "+longLink.getHead().toString();
	}
		
	/** Remove a link from the merge
	 * @param l the link to be removed
	 * @return whether there is one link remaining
	 */
	public boolean remove(Link l) {
		if (shortLink != null && shortLink.equals(l)) {
			shortLink = null;
		}
		else if (longLink != null && longLink.equals(l)) {
			longLink = null;
		}
		if (!super.remove(l)) 
			throw new RuntimeException("A Link was removed that wasn't in the BushMerge");
		if (size() < 2) return true;
		return false;
	}
	
	/** get a Link´s share of the merge demand
	 * @param l the link whose split should be returned
	 * @return the share of the demand through this node carried by the Link
	 */
	public Float getSplit(Link l) {
		Float r = split.getOrDefault(l, 0.0F);
		if (r.isNaN()) {	//NaN check
			throw new RuntimeException("BushMerge split is NaN");
		}
		return r;
	}

	/**Get the maximum flow that can be shifted from the longest to shortest cost path
	 * @param bushFlows	the current flows on the Bush
	 * @return the nmax flow that can be shifted away from the longest path
	 */
	public double getMaxDelta(Map<Link, Double> bushFlows) {
		//Find the start and end of the ASP
		Node cur = longLink.getHead();
		Node stop = bush.divergeNode(cur);
		//Check all links in the longest path, taking the smallest Bush flow
		Double max = null;
		while (cur != stop) {
			Link ll = bush.getqLong(cur);
			if (max == null) max = bushFlows.get(ll);
			else max = Math.min(max,bushFlows.get(ll));
			cur = ll.getTail();
		}
		return max;
	}

	/**Set the split for a given link
	 * @param l	the link whose split should be set
	 * @param d	the split value
	 * @return	the previous value, or 0.0 if the link wasn't in the Merge before
	 */
	public Float setSplit(Link l, Float d) {
		if (d.isNaN()) {
			throw new RuntimeException("BushMerge split set to NaN");
		}
		Float val = split.put(l, d);
		return val == null? 0.0F : val;
	}
}
