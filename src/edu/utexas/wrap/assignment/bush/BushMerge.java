package edu.utexas.wrap.assignment.bush;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.Node;

public class BushMerge extends HashSet<Link> implements BackVector{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Link shortLink;
	private Link longLink;
	private Node diverge;
	private final Map<Link, Double> split;
	private final Bush bush;
	
	/** Create a BushMerge by adding a shortcut link to a node
	 * @param b the bush upon whose structure the merge depends
	 * @param u the pre-existing back-vector that is short-circuited
	 * @param l the shortcut link providing a shorter path to the node
	 */
	public BushMerge(Bush b, Link u, Link l) {
		bush = b;
		longLink = u;
		shortLink = l;
		diverge = b.divergeNode(l.getHead());
		split = new HashMap<Link, Double>();
		split.put(l, 1.0);
	}
	
	public Link getShortLink() {
		if (shortLink == null) throw new RuntimeException("Shortest path search wasn't run yet!");
		return shortLink;
	}
	
	public Link getLongLink() {
		if (longLink == null) throw new RuntimeException("Longest path search wasn't run yet!");
		return longLink;
	}
	
	public Node getDiverge() {
		return diverge;
	}
	
	protected void setShortLink(Link l) {
		shortLink = l;
		diverge = bush.divergeNode(longLink.getHead());
	}
	
	protected void setLongLink(Link l) {
		longLink = l;
		diverge = bush.divergeNode(shortLink.getHead());
	}
	
	protected void setDiverge(Node n) {
		diverge = n;
	}
	
	public String toString() {
		return "Merge from diverge "+diverge.toString();
	}
		
	/** Remove a link from the merge
	 * @param l the link to be removed
	 * @return whether there is one link remaining
	 */
	public boolean remove(Link l) {
		if (shortLink.equals(l)) {
			shortLink = null;
		}
		else if (longLink.equals(l)) {
			longLink = null;
		}
		if (!super.remove(l)) throw new RuntimeException("A Link was removed that wasn't in the BushMerge");
		if (size() < 2) return true;
		return false;
	}
	
	public double getSplit(Link l) {
		return split.getOrDefault(l, 0.0);
	}
}
