package edu.utexas.wrap.distribution;

import edu.utexas.wrap.demand.OriginDestinationMatrix;
import edu.utexas.wrap.demand.ProductionAttractionMap;

public abstract class TripDistributor {

	public abstract OriginDestinationMatrix distribute(ProductionAttractionMap pa);
	
}
