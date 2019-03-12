package edu.utexas.wrap.modechoice;

import java.util.Set;

import edu.utexas.wrap.demand.AggregateProductionAttractionMatrix;
import edu.utexas.wrap.demand.ModalProductionAttractionMatrix;

public abstract class TripInterchangeSplitter {
	
	public abstract Set<ModalProductionAttractionMatrix> split(AggregateProductionAttractionMatrix aggregate);
	
}
