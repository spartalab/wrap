package edu.utexas.wrap.demand;

import edu.utexas.wrap.modechoice.Mode;

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

	/**
	 * @return the Mode associated with this matrix
	 */
	public Mode getMode();
}
