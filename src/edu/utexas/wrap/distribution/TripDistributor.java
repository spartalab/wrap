package edu.utexas.wrap.distribution;

import edu.utexas.wrap.OriginDestinationMatrix;
import edu.utexas.wrap.ProductionAttractionMap;

public abstract class TripDistributor {

	public abstract OriginDestinationMatrix distribute(ProductionAttractionMap pa);
	
}
