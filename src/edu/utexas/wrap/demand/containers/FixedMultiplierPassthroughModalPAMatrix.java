package edu.utexas.wrap.demand.containers;

import java.util.Collection;
import java.util.Collections;

import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.ModalPAMatrix;
import edu.utexas.wrap.demand.PAMatrix;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.TravelSurveyZone;

public class FixedMultiplierPassthroughModalPAMatrix implements ModalPAMatrix {
	private final float multiplier;
	private final Mode mode;
	private final PAMatrix aggregate;
	
	public FixedMultiplierPassthroughModalPAMatrix(Mode m, double pct, PAMatrix agg) {
		multiplier = (float) pct;
		mode = m;
		aggregate = agg;
	}
	
	public FixedMultiplierPassthroughModalPAMatrix(double pct, ModalPAMatrix modalMatrix) {
		multiplier = (float) pct;
		mode = modalMatrix.getMode();
		aggregate = modalMatrix;
	}
	
	@Override
	public Collection<TravelSurveyZone> getZones() {
		return aggregate.getZones();
	}

	@Override
	public void put(TravelSurveyZone producer, TravelSurveyZone attractor, Float demand) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Writing to a read-only matrix");
	}

	@Override
	public float getDemand(TravelSurveyZone producer, TravelSurveyZone attractor) {
		return multiplier*aggregate.getDemand(producer, attractor);
	}

	@Override
	public DemandMap getDemandMap(TravelSurveyZone producer) {
		return new FixedMultiplierPassthroughDemandMap(aggregate.getDemandMap(producer),multiplier);
	}

	@Override
	public Collection<TravelSurveyZone> getProducers() {
		return multiplier <= 0? Collections.<TravelSurveyZone>emptySet() : aggregate.getProducers();
	}

	@Override
	public Mode getMode() {
		return mode;
	}

}
