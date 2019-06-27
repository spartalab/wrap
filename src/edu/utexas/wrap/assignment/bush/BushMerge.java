package edu.utexas.wrap.assignment.bush;

import java.util.Iterator;
import java.util.Set;

import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.Node;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;

/**A way to represent the Links which merge at a given Node in a Bush,
 * namely, a set of Links with a longest and shortest path named and a
 * Map of each link's share of the total load
 * @author William
 *
 */
public class BushMerge extends Object2FloatOpenHashMap<Link> implements BackVector, Iterable<Link>{
	/**
	 * 
	 */
	private static final long serialVersionUID = 497136906150582949L;
	private Link shortLink;
	private Link longLink;
	private final Bush bush;
	private final Node head;
	
	/** Create a BushMerge by adding a shortcut link to a node
	 * @param b the bush upon whose structure the merge depends
	 * @param u the pre-existing back-vector that is short-circuited
	 * @param l the shortcut link providing a shorter path to the node
	 */
	public BushMerge(Bush b, Link u, Link l) {
		this(b,u == null? l.getHead() : u.getHead());

		put(u, 1.0F);
		put(l, 0.0F);
	}
	
	/**Duplication constructor
	 * @param bm	the BushMerge to be copied
	 */
	public BushMerge(BushMerge bm) {
		super(bm);
		bush = bm.bush;
		longLink = bm.longLink;
		shortLink = bm.shortLink;
		this.head = bm.head;
		defaultReturnValue(-1F);
	}
	
	/**Constructor for empty merge
	 * @param b
	 */
	protected BushMerge(Bush b, Node n) {
		super(3,1.0f);
		defaultReturnValue(-1F);
		bush = b;
		head = n;
	}
	
	/**
	 * @return the shortest cost path Link
	 */
	@Override
	public Link getShortLink() {
		return shortLink;
	}
	
	/**
	 * @return the longest cost path Link
	 */
	@Override
	public Link getLongLink() {
		return longLink;
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
		if (super.removeFloat(l) == defaultReturnValue()) 
			throw new RuntimeException("A Link was removed that wasn't in the BushMerge");
		if (size() == 1) return true;
		trim();
		return false;
	}
	
	/** get a Link´s share of the merge demand
	 * @param l the link whose split should be returned
	 * @return the share of the demand through this node carried by the Link
	 */
	public Float getSplit(Link l) {
		Float r = getOrDefault(l, 0.0F);
		if (r.isNaN()) {	//NaN check
			throw new RuntimeException("BushMerge split is NaN");
		}
		return r;
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
		Float val = put(l, d.floatValue());
		return val == null? 0.0F : val;
	}

	@Override
	public Iterator<Link> iterator() {
		return keySet().iterator();
	}

	/**Add a link to the BushMerge
	 * @param l the link to be added
	 * @return whether the link was successfully added
	 */
	public Boolean add(Link l) {
		try { 
			put(l, 0.0F);
			trim();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * @param link the link which may be in the BushMerge
	 * @return whether the link is in the BushMerge
	 */
	public boolean contains(Link link) {
		return containsKey(link);
	}

	/**
	 * @return the set of links in this BushMerge
	 */
	public Set<Link> getLinks() {
		return keySet();
	}

	@Override
	public Node getHead() {
		return head;
	}
	
}
