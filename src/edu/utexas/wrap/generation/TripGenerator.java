package edu.utexas.wrap.generation;


import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.net.Demographic;

public interface TripGenerator {
	
	public DemandMap generate(Demographic attractionDemographic);
}
