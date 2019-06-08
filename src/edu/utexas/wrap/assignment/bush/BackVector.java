package edu.utexas.wrap.assignment.bush;

import edu.utexas.wrap.net.Link;

/** A BackVector is the object through which all
 * Links flowing into a Node from a Bush are enumerated
 * @author William
 *
 */
public interface BackVector {

	public Link getShortLink();
	
	public Link getLongLink();
}
