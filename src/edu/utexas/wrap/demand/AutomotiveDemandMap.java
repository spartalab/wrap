package edu.utexas.wrap.demand;

import edu.utexas.wrap.VehicleClass;

public class AutomotiveDemandMap extends DemandMap {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2176027918628424731L;
	private final AutomotiveOriginDestinationMatrix parent;
	
	public AutomotiveDemandMap(AutomotiveOriginDestinationMatrix parent) {
		this.parent = parent;
	}

	public VehicleClass getVehicleClass() {
		// TODO Auto-generated method stub
		return parent.getVehicleClass();
	}

	public Float getVOT() {
		// TODO Auto-generated method stub
		return parent.getVOT();
	}

}
