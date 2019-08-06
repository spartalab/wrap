package edu.utexas.wrap.modechoice;

import edu.utexas.wrap.demand.ModalPAMap;
import edu.utexas.wrap.demand.PAMap;

public abstract class TripEndSplitter {
	
	public abstract ModalPAMap split(PAMap map);
}
