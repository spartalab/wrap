package edu.utexas.wrap.demand;

import edu.utexas.wrap.modechoice.Mode;

/**An extension of demand maps 
 * @author William
 *
 */
public class AutomotiveDemandMap extends DemandMap {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2176027918628424731L;
	private final AutomotiveOriginDestinationMatrix parent;
	
	public AutomotiveDemandMap(AutomotiveOriginDestinationMatrix parent) {
		this.parent = parent;
	}

	public AutomotiveDemandMap(DemandMap sub, AutomotiveOriginDestinationMatrix parent) {
		super(sub);
		this.parent = parent;
	}
	public Mode getMode() {
		// TODO Auto-generated method stub
		return parent.getMode();
	}

	public Float getVOT() {
		// TODO Auto-generated method stub
		return parent.getVOT();
	}

}
