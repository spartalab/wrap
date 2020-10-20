package edu.utexas.wrap.demand.containers;

import java.util.Collection;

import edu.utexas.wrap.TimePeriod;
import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.TravelSurveyZone;

public class AddingPassthroughODMatrix implements ODMatrix {

	private final ODMatrix od1, od2;
	private TimePeriod tp;
	
	public AddingPassthroughODMatrix(ODMatrix od1, ODMatrix od2) {
		this.od1 = od1;
		this.od2 = od2;
	}
	
	@Override
	public Mode getMode() {
		return od1.getMode();
	}

	@Override
	public float getDemand(TravelSurveyZone origin, TravelSurveyZone destination) {
		// TODO Auto-generated method stub
		return od1.getDemand(origin, destination) + od2.getDemand(origin, destination);
	}

	@Override
	public void put(TravelSurveyZone origin, TravelSurveyZone destination, Float demand) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Read-only matrix");
	}

	@Override
	public Float getVOT() {
		// TODO Auto-generated method stub
		return od1.getVOT();
	}

	@Override
	public void setVOT(float VOT) {
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
		return od1.getZones();
	}

	@Override
	public DemandMap getDemandMap(TravelSurveyZone origin) {
		// TODO Auto-generated method stub
		throw new RuntimeException();
	}

}
