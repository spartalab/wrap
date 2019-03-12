package edu.utexas.wrap.modechoice;

import java.util.Set;

import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.ModalPAMatrix;

public abstract class TripInterchangeSplitter {
	
	public abstract Set<ModalPAMatrix> split(AggregatePAMatrix aggregate);
	
}
