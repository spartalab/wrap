package edu.utexas.wrap.demand;

import edu.utexas.wrap.VehicleClass;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Node;
/** A mode-specific OD matrix for autos/trucks only.
 * Instances of this class are expected by a 
 * @author William
 *
 */
public class AutomotiveOriginDestinationMatrix extends ModalOriginDestinationMatrix {
	private final Float vot;
	private final VehicleClass c;
	
	public AutomotiveOriginDestinationMatrix(Float vot, VehicleClass c) {
		super(Mode.AUTO);
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

	public AutomotiveDemandMap get(Node origin) {
		return new AutomotiveDemandMap(get(origin), this);
	}
	
	@Override
	public void put(Node origin, Node destination, Float demand) {
		// TODO Auto-generated method stub
		putIfAbsent(origin, new AutomotiveDemandMap(this));
		get(origin).put(destination, demand);
	}

}
