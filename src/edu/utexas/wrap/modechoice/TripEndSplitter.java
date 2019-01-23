package edu.utexas.wrap.modechoice;

import edu.utexas.wrap.demand.ModalProductionAttractionMap;
import edu.utexas.wrap.demand.ProductionAttractionMap;

public abstract class TripEndSplitter {

	
	public abstract ModalProductionAttractionMap split(ProductionAttractionMap map);
}
