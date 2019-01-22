package edu.utexas.wrap.modechoice;

import edu.utexas.wrap.ModalOriginDestinationMatrix;
import edu.utexas.wrap.OriginDestinationMatrix;

public abstract class TripInterchangeSplitter {

	public abstract ModalOriginDestinationMatrix split(OriginDestinationMatrix aggregate);
	
}
