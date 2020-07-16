package edu.utexas.wrap.demand.containers;

import java.util.Collection;
import java.util.HashSet;

import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.ModalPAMatrix;
import edu.utexas.wrap.demand.PAMatrix;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.TravelSurveyZone;

public class ModalFixedMultiplierPassthroughMatrix implements ModalPAMatrix {
	private float percent;
	private Mode mode;
	private PAMatrix aggregate;
	public ModalFixedMultiplierPassthroughMatrix(Mode m, double pct, PAMatrix agg) {
		percent = (float) pct;
		mode = m;
		aggregate = agg;
	}
	
	public ModalFixedMultiplierPassthroughMatrix(double pct, ModalPAMatrix modalMatrix) {
		percent = (float) pct;
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
		return percent*aggregate.getDemand(producer, attractor);
	}

	@Override
	public DemandMap getDemandMap(TravelSurveyZone producer) {
		return new FixedMultiplierPassthroughDemandMap(aggregate.getDemandMap(producer),percent);
	}

	@Override
	public Collection<TravelSurveyZone> getProducers() {
		return percent <= 0? new HashSet<TravelSurveyZone>() : aggregate.getProducers();
	}

	@Override
	public Mode getMode() {
		return mode;
	}

}
