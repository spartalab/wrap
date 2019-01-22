package edu.utexas.wrap;

import java.util.HashMap;
import java.util.Map;

import edu.utexas.wrap.net.Node;

public class DemandMap extends HashMap<Node, Float>{
	private final OriginDestinationMatrix parent;

	public DemandMap(OriginDestinationMatrix parent) {
		this.parent = parent;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -8268461681839852205L;
	
	public Float getVOT() {
		return parent == null? 1.0F : parent.getVOT();
	}
	
	public VehicleClass getVehicleClass() {
		return parent == null? null : parent.getVehicleClass();
	}
}
