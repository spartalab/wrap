package edu.utexas.wrap.modechoice;

import edu.utexas.wrap.demand.ModalPAMap;
import edu.utexas.wrap.demand.PAMap;

/**An interface for performing mode choice before distribution
 * 
 * @author William
 *
 */
public abstract class TripEndSplitter {
	
	public abstract ModalPAMap split(PAMap map);
}
