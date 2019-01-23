package edu.utexas.wrap.modechoice;

import edu.utexas.wrap.demand.ModalOriginDestinationMatrix;
import edu.utexas.wrap.demand.OriginDestinationMatrix;

public abstract class TripInterchangeSplitter {

	public abstract ModalOriginDestinationMatrix split(OriginDestinationMatrix aggregate);
	
}
