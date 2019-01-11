package edu.utexas.wrap;

public class AutomotiveOriginDestinationMatrix extends ModalOriginDestinationMatrix {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5344167950596246262L;

	public AutomotiveOriginDestinationMatrix(OriginDestinationMatrix od) {
		super(Mode.AUTO, od);
	}

}
