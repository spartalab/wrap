package edu.utexas.wrap.demand.containers;

import java.util.Collection;

import edu.utexas.wrap.TimePeriod;
import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.TravelSurveyZone;

public class FixedMultiplierPassthroughODMatrix implements ODMatrix {

	private final ODMatrix parent;
	private final float multiplier;
	private TimePeriod tp;
	
	public FixedMultiplierPassthroughODMatrix(ODMatrix parent, float multiplier) {
		this.parent = parent;
		this.multiplier = multiplier;
	}
	
	@Override
	public Mode getMode() {
		// TODO Auto-generated method stub
		return parent.getMode();
	}

	@Override
	public float getDemand(TravelSurveyZone origin, TravelSurveyZone destination) {
		// TODO Auto-generated method stub
		return parent.getDemand(origin, destination) * multiplier;
	}

	@Override
	public void put(TravelSurveyZone origin, TravelSurveyZone destination, Float demand) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Read-only matrix");
	}

	@Override
	public TimePeriod timePeriod() {
		// TODO Auto-generated method stub
		return tp;
	}

	@Override
	public Collection<TravelSurveyZone> getZones() {
		// TODO Auto-generated method stub
		return parent.getZones();
	}

	@Override
	public DemandMap getDemandMap(TravelSurveyZone origin) {
		// TODO Auto-generated method stub
		throw new RuntimeException();
	}

}
