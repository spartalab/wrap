package edu.utexas.wrap.modechoice;

import java.util.Map;

import edu.utexas.wrap.demand.AggregateProductionAttractionMatrix;
import edu.utexas.wrap.demand.ModalProductionAttractionMatrix;

public abstract class TripInterchangeSplitter {
	
	public abstract Map<Mode, ModalProductionAttractionMatrix> split(AggregateProductionAttractionMatrix aggregate);
	
}
