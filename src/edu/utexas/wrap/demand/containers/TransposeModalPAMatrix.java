package edu.utexas.wrap.demand.containers;

import java.util.Collection;

import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.ModalPAMatrix;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.TravelSurveyZone;

public class TransposeModalPAMatrix implements ModalPAMatrix {
	private final ModalPAMatrix base;
	
	public TransposeModalPAMatrix(ModalPAMatrix pa) {
		base = pa;
	}

	@Override
	public void put(TravelSurveyZone producer, TravelSurveyZone attractor, Float demand) {
		throw new RuntimeException("Writing to read-only matrix");
	}

	@Override
	public float getDemand(TravelSurveyZone producer, TravelSurveyZone attractor) {
		return base.getDemand(attractor, producer);
	}

	@Override
	public DemandMap getDemandMap(TravelSurveyZone producer) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public Collection<TravelSurveyZone> getProducers() {
		// TODO Auto-generated method stub
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public Mode getMode() {
		return base.getMode();
	}
	
	@Override
	public Collection<TravelSurveyZone> getZones(){
		return base.getZones();
	}

}
