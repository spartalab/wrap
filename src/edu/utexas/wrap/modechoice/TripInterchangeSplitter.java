package edu.utexas.wrap.modechoice;

import java.util.Map;
import java.util.Set;

import edu.utexas.wrap.demand.AggregateOriginDestinationMatrix;
import edu.utexas.wrap.demand.ModalOriginDestinationMatrix;

public abstract class TripInterchangeSplitter {
	
	public abstract Map<Mode, ModalOriginDestinationMatrix> split(AggregateOriginDestinationMatrix aggregate);
	
}
