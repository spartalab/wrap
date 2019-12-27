package edu.utexas.wrap.assignment.bush;

import java.util.stream.Stream;

import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.Node;

/**A way to represent the Links which merge at a given Node in a Bush,
 * namely, a set of Links with a longest and shortest path named and a
 * Map of each link's share of the total load
 * @author William
 *
 */
public class BushMerge implements BackVector {
	private Float[] shares;
	private Link shortLink;
	private Link longLink;
	private final Node head;
	
	/** Create a BushMerge by adding a shortcut link to a node
	 * @param b the bush upon whose structure the merge depends
	 * @param u the pre-existing back-vector that is short-circuited
	 * @param l the shortcut link providing a shorter path to the node
	 */
	public BushMerge(Bush b, Link u, Link l) {
		this(b,u == null? l.getHead() : u.getHead());
		shares[u.getHead().orderOf(u)] = 1.0f;
		shares[l.getHead().orderOf(l)] = 0.0f;
	}
	
	/**Duplication constructor
	 * @param bm	the BushMerge to be copied
	 */
	public BushMerge(BushMerge bm) {
		longLink = bm.longLink;
		shortLink = bm.shortLink;
		this.head = bm.head;
		shares = bm.shares;
	}
	
	public BushMerge(BushMerge bm, Node n) {
		head = n;
		shares = bm.shares;
	}
	
	/**Constructor for empty merge
	 * @param b
	 */
	protected BushMerge(Bush b, Node n) {
		shares = new Float[n.reverseStar().length];
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
		int idx = head.orderOf(l);

		if (shares[idx] == null) 
			throw new RuntimeException("A Link was removed that wasn't in the BushMerge");
		shares[idx] = null;
		if (Stream.of(shares).filter(x -> x != null).count() == 1) return true;
		return false;
	}
	
	/** get a Link´s share of the merge demand
	 * @param l the link whose split should be returned
	 * @return the share of the demand through this node carried by the Link
	 */
	public Double getSplit(Link l) {
		int idx = head.orderOf(l);
		Double r = shares[idx] == null? 0.0 : shares[idx];
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
	public Double setSplit(Link l, Double d) {
		if (d.isNaN()) {
			throw new RuntimeException("BushMerge split set to NaN");
		}
		Float val = shares[head.orderOf(l)];
		shares[head.orderOf(l)] = d.floatValue();
		return val == null? 0.0 : val;
	}

//	@Override
//	public Iterator<Link> iterator() {
//		return getLinks().iterator();
//	}

	/**Add a link to the BushMerge
	 * @param l the link to be added
	 * @return whether the link was successfully added
	 */
	public Boolean add(Link l) {
		int idx = head.orderOf(l);
		if (idx < 0 || idx > head.reverseStar().length) return false;
		shares[idx] = 0.0f;
		return true;

	}

	/**
	 * @param link the link which may be in the BushMerge
	 * @return whether the link is in the BushMerge
	 */
//	public boolean contains(Link link) {
//		return containsKey(link);
//	}

	/**
	 * @return the set of links in this BushMerge
	 */
	public Stream<Link> getLinks() {
		return Stream.of(head.reverseStar()).filter(x -> shares[head.orderOf(x)] != null);
	}

	@Override
	public Node getHead() {
		return head;
	}

	/**
	 * @return the number of active links in the merge
	 */
	public int size() {
		// TODO Auto-generated method stub
		return (int) Stream.of(shares).filter(x -> x != null).count();
	}

	/**
	 * @param l a link to check if it is present
	 * @return whether the link is active in the bush
	 */
	public boolean contains(Link l) {
		int idx = head.orderOf(l);
		if (idx < 0) return false;
		return shares[idx] != null;
	}
	
	/**
	 * remove previously-assigned backvector labels
	 */
	public void clearLabels() {
		shortLink = null;
		longLink = null;
	}
}
