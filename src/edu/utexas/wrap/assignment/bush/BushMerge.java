package edu.utexas.wrap.assignment.bush;

import java.util.HashSet;

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
		diverge = b.divergeNode(l.getTail(), u.getTail());
	}
	
	public Link getShortLink() {
		return shortLink;
	}
	
	public Link getLongLink() {
		return longLink;
	}
	
	public Node getDiverge() {
		return diverge;
	}
	
	protected void setShortLink(Link l) {
		shortLink = l;
	}
	
	protected void setLongLink(Link l) {
		longLink = l;
	}
	
	protected void setDiverge(Node n) {
		diverge = n;
	}
	
	public String toString() {
		return "Merge from diverge "+diverge.toString();
	}
	
	//TODO handle adding links
	@Override
	public boolean add(Link l) {
		
	}
	
	/** Remove a link from the merge
	 * @param l the link to be removed
	 * @return whether there is one link remaining
	 */
	@Override
	public boolean remove(Link l) {
		if (shortLink.equals(l)) {
			//TODO
		}
		else if (longLink.equals(l)) {
			//TODO
		}
		if (!super.remove(l)) throw new RuntimeException("A Link was removed that wasn't in the BushMerge");
		if (size() < 2) return true;
	}
}
