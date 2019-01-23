package edu.utexas.wrap.demand;

import java.util.HashMap;

import edu.utexas.wrap.Mode;
import edu.utexas.wrap.VehicleClass;
import edu.utexas.wrap.net.Node;
/** A mode-specific OD matrix for autos/trucks only.
 * Instances of this class are expected by a 
 * @author William
 *
 */
public class AutomotiveOriginDestinationMatrix extends HashMap<Node, AutomotiveDemandMap> implements ModalOriginDestinationMatrix {
	private final Float vot;
	private final VehicleClass c;
	private final Mode mode = Mode.AUTO;
	
	public AutomotiveOriginDestinationMatrix(Float vot, VehicleClass c) {
		this.vot = vot;
		this.c = c;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -5344167950596246262L;

	public VehicleClass getVehicleClass() {
		return c;
	}

	public Float getVOT() {
		return vot;
	}

	@Override
	public Float getDemand(Node origin, Node destination) {
		return get(origin) == null? 0.0F : get(origin).getOrDefault(destination,0.0F);
	}

	@Override
	public void put(Node origin, Node destination, Float demand) {
		// TODO Auto-generated method stub
		putIfAbsent(origin, new AutomotiveDemandMap(this));
		get(origin).put(destination, demand);
	}

	@Override
	public Mode getMode() {
		return mode;
	}
}
