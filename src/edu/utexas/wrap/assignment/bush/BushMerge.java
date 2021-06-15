/*
    wrap - free and open-source urban transportation modeling software
    Copyright (C) 2017 the wrap project, The University of Texas at Austin

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.utexas.wrap.assignment.bush;

import java.util.List;
import java.util.Map.Entry;
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
	private Double[] shares;
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
		shares[u.getHead().orderOf(u)] = 1.0;
		shares[l.getHead().orderOf(l)] = 0.0;
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
		shares = new Double[n.reverseStar().length];
		head = n;
	}
	
	public BushMerge(Node head, List<Entry<Link, Double>> value) {
		// TODO Auto-generated constructor stub
		this.head = head;
		shares = new Double[head.reverseStar().length];
		value.forEach(entry -> setSplit(entry.getKey(),entry.getValue()));
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
		Double val = shares[head.orderOf(l)];
		shares[head.orderOf(l)] = d;
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
		shares[idx] = 0.0;
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
