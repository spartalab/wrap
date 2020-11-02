package edu.utexas.wrap.demand.containers;

import java.util.Collection;

import edu.utexas.wrap.TimePeriod;
import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.TravelSurveyZone;

public class TransposeODMatrix implements ODMatrix {

	private final ODMatrix parent;
	
	public TransposeODMatrix(ODMatrix parent) {
		this.parent = parent;
	}
	
	@Override
	public Mode getMode() {
		return parent.getMode();
	}

	@Override
	public float getDemand(TravelSurveyZone origin, TravelSurveyZone destination) {
		return parent.getDemand(destination, origin);
	}

	@Override
	public void put(TravelSurveyZone origin, TravelSurveyZone destination, Float demand) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Read-only matrix");
	}

	@Override
	public Collection<TravelSurveyZone> getZones() {
		return parent.getZones();
	}
	
	@Override
	public TimePeriod timePeriod() {
		// TODO Auto-generated method stub
		return parent.timePeriod();
	}

	@Override
	public DemandMap getDemandMap(TravelSurveyZone origin) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Undefined");
	}

}
