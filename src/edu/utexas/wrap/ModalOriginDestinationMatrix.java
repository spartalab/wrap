package edu.utexas.wrap;

public abstract class ModalOriginDestinationMatrix extends OriginDestinationMatrix {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1522282834429893220L;
	
	private Mode mode;
	
	public ModalOriginDestinationMatrix(Mode mode, OriginDestinationMatrix od) {
		super(od);
		this.mode = mode;
		
	}
	
	public Mode getMode() {
		return mode;
	}

}
