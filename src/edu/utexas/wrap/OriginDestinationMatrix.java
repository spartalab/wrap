package edu.utexas.wrap;

import java.util.HashMap;
import edu.utexas.wrap.net.Node;

public class OriginDestinationMatrix extends HashMap<Node, DemandMap>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8451483491603490459L;
	private final Float vot;
	private final VehicleClass c;

	public OriginDestinationMatrix(Float vot, VehicleClass c) {
		// TODO Auto-generated constructor stub
		this.vot = vot;
		this.c = c;
	}

	public VehicleClass getVehicleClass() {
		return c;
	}

	public Float getVOT() {
		return vot;
	}
}
