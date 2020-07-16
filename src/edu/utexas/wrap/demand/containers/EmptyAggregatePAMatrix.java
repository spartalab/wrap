package edu.utexas.wrap.demand.containers;

import java.util.Collection;
import java.util.Collections;

import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.net.TravelSurveyZone;

public class EmptyAggregatePAMatrix implements AggregatePAMatrix {

	

	@Override
	public void put(TravelSurveyZone producer, TravelSurveyZone attractor, Float demand) {
		throw new RuntimeException("Writing to read-only matrix");
	}

	@Override
	public float getDemand(TravelSurveyZone producer, TravelSurveyZone attractor) {
		return 0.0f;
	}

	@Override
	public DemandMap getDemandMap(TravelSurveyZone producer) {
		return new EmptyDemandMap();
	}

	@Override
	public Collection<TravelSurveyZone> getProducers() {
		return Collections.emptySet();
	}

	@Override
	public Collection<TravelSurveyZone> getZones() {
		// TODO Auto-generated method stub
		return Collections.emptySet();
	}

}
