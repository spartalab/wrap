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

	public Mode getMode();

	public Float getVOT() ;

}
