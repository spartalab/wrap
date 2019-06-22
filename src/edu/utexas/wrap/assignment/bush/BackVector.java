package edu.utexas.wrap.assignment.bush;

import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.Node;

/** A BackVector is the object through which all
 * Links flowing into a Node from a Bush are enumerated
 * @author William
 *
 */
public interface BackVector {

	/**
	 * @return the link along the shortest path back to the origin
	 */
	public Link getShortLink();
	
	/**
	 * @return the link along the longest path back to the origin
	 */
	public Link getLongLink();
	
	/**
	 * @return the node where this Backvector leads
	 */
	public Node getHead();
}
