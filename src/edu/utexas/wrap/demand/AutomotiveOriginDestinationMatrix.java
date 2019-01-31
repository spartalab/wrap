package edu.utexas.wrap.demand;

import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Node;
/** A mode-specific OD matrix for autos/trucks only.
 * Instances of this class are expected by a 
 * @author William
 *
 */
public class AutomotiveOriginDestinationMatrix extends ModalOriginDestinationMatrix {
	private final Float vot;
	
	public AutomotiveOriginDestinationMatrix(Float vot, Mode c) {
		super(c);
		this.vot = vot;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -5344167950596246262L;

	public Float getVOT() {
		return vot;
	}

	public AutomotiveDemandMap get(Node origin) {
		//TODO: WTF is this, William?
		return new AutomotiveDemandMap(super.get(origin), this);
	}
	
	@Override
	public void put(Node origin, Node destination, Float demand) {
		putIfAbsent(origin, new AutomotiveDemandMap(this));
		get(origin).put(destination, demand);
	}

}
