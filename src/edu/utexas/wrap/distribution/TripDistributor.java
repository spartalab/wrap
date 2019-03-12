package edu.utexas.wrap.distribution;

import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.PAMap;

public abstract class TripDistributor {

	public abstract AggregatePAMatrix distribute(PAMap pa);
	
}
