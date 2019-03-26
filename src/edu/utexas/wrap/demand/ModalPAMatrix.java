package edu.utexas.wrap.demand;

import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Node;

/**This is used after mode choice and distribution
 * to map from a zone to its production and attraction
 * values for a particular travel mode.
 * 
 * Any ModalPAMatrix should be able to retrieve metadata
 * useful in subsequent steps, e.g. the VOT for route
 * choice.
 * 
 * @author William
 *
 */
public interface ModalPAMatrix extends PAMatrix {

	public Mode getMode();

	public void put(Node origin, Node destination, Float demand);
}
