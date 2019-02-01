package edu.utexas.wrap.distribution;

import edu.utexas.wrap.demand.AggregateProductionAttractionMatrix;
import edu.utexas.wrap.demand.ProductionAttractionMap;

public abstract class TripDistributor {

	public abstract AggregateProductionAttractionMatrix distribute(ProductionAttractionMap pa);
	
}
