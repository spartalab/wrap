package edu.utexas.wrap.demand.containers;

import java.util.Collection;
import java.util.HashSet;

import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.net.TravelSurveyZone;

public class AggregateFixedMultiplierPassthroughMatrix implements AggregatePAMatrix {
	
	private final AggregatePAMatrix base;
	private final float multip;

	public AggregateFixedMultiplierPassthroughMatrix(AggregatePAMatrix baseMatrix, float multiplier) {
		base = baseMatrix;
		multip = multiplier;
	}
	
	@Override
	public Collection<TravelSurveyZone> getZones() {
		return base.getZones();
	}

	@Override
	public void put(TravelSurveyZone producer, TravelSurveyZone attractor, Float demand) {
		throw new RuntimeException("Writing to a read-only matrix");
	}

	@Override
	public float getDemand(TravelSurveyZone producer, TravelSurveyZone attractor) {
		return multip*base.getDemand(producer, attractor);
	}

	@Override
	public DemandMap getDemandMap(TravelSurveyZone producer) {
		return new FixedMultiplierPassthroughDemandMap(base.getDemandMap(producer),multip);
	}

	@Override
	public Collection<TravelSurveyZone> getProducers() {
		return multip <= 0? new HashSet<TravelSurveyZone>() : base.getProducers();
	}

}
