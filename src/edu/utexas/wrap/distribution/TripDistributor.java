package edu.utexas.wrap.distribution;

import edu.utexas.wrap.demand.AggregateOriginDestinationMatrix;
import edu.utexas.wrap.demand.ProductionAttractionMap;

public abstract class TripDistributor {

	public abstract AggregateOriginDestinationMatrix distribute(ProductionAttractionMap pa);
	
}
