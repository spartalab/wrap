package edu.utexas.wrap.assignment.bush;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.Node;
import edu.utexas.wrap.util.AlternateSegmentPair;

public class BushMerge extends HashSet<Link> implements BackVector{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Link shortLink;
	private Link longLink;
	private AlternateSegmentPair asp;
	private Map<Link, Double> split;
	private final Bush bush;
	
	/** Create a BushMerge by adding a shortcut link to a node
	 * @param b the bush upon whose structure the merge depends
	 * @param u the pre-existing back-vector that is short-circuited
	 * @param l the shortcut link providing a shorter path to the node
	 */
	public BushMerge(Bush b, Link u, Link l) {
		bush = b;
//		longLink = u;
//		shortLink = l;
		add(l);
		add(u);
//		asp = b.getShortLongASP(l.getHead());
		split = new HashMap<Link, Double>();
		split.put(u, 1.0);
	}
	
	public BushMerge(BushMerge bm) {
		super(bm);
		bush = bm.bush;
		longLink = bm.longLink;
		shortLink = bm.shortLink;
		asp = bm.asp;
		split = new HashMap<Link,Double>(bm.split);
	}
	
	public Link getShortLink() {
//		if (shortLink == null) throw new RuntimeException("Shortest path search wasn't run yet!");
		return shortLink;
	}
	
	public Link getLongLink() {
//		if (longLink == null) throw new RuntimeException("Longest path search wasn't run yet!");
		return longLink;
	}
	
	public AlternateSegmentPair getShortLongASP() {
		if (asp == null) asp = bush.getShortLongASP(shortLink.getHead());
		return asp;
	}
	
	protected void setShortLink(Link l) {
		shortLink = l;
//		asp = bush.getShortLongASP(shortLink.getHead());
	}
	
	protected void setLongLink(Link l) {
		longLink = l;
//		asp = bush.getShortLongASP(longLink.getHead());
	}
	

	
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
	
	public double getSplit(Link l) {
		Double r = split.getOrDefault(l, 0.0);
		if (r.isNaN()) {
			throw new RuntimeException("BushMerge split is NaN");
		}
		return r;
	}

	public double getMaxDelta(Map<Link, Double> bushFlows) {
		// TODO Auto-generated method stub
		Node cur = longLink.getHead();
		Node stop = bush.divergeNode(cur);
		Double max = null;
		while (cur != stop) {
			Link ll = bush.getqLong(cur);
			if (max == null) max = bushFlows.get(ll);
			else max = Math.min(max,bushFlows.get(ll));
			cur = ll.getTail();
		}
		return max;
	}

	public double setSplit(Link l, double d) {
		if (((Double) d).isNaN()) {
			throw new RuntimeException("BushMerge split set to NaN");
		}
		Double val = split.put(l, d);
		return val == null? 0.0 : val;
	}
}
