package edu.utexas.wrap.demand;

import edu.utexas.wrap.modechoice.Mode;

/** A container for mapping production and attraction values to
 * specified zones that use the specified mode.
 * 
 * This will be used in trip-end splitting (not trip-interchange).
 * 
 * @author William
 *
 */
public interface ModalPAMap extends PAMap {

	/**
	 * @return the Mode of transportation associated with this map
	 */
	public Mode getMode();

	/**
	 * @return the VOT associated with trips stored in the Map
	 */
	public Float getVOT();
}
