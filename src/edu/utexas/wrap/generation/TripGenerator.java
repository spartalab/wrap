package edu.utexas.wrap.generation;

import edu.utexas.wrap.demand.DemandMap;

public interface TripGenerator {

	public DemandMap generate(GenerationRate rate);
}
