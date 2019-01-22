package edu.utexas.wrap;

public abstract class ModalOriginDestinationMatrix extends OriginDestinationMatrix {

	public ModalOriginDestinationMatrix(Float vot, VehicleClass c) {
		super(vot, c);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -1522282834429893220L;
	
	private Mode mode;
	
 	public Mode getMode() {
		return mode;
	}

}
