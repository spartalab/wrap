package edu.utexas.wrap.demand;

import edu.utexas.wrap.modechoice.Mode;

/**DemandMap that can be loaded onto a roadway network,
 * featuring a value of time and a mode
 * @author William
 *
 */
public interface AutoDemandMap extends DemandMap {
	
	/**
	 * @return the value of time
	 */
	public Float getVOT();
	
	/**
	 * @return the mode of transport
	 */
	public Mode getMode();
}
