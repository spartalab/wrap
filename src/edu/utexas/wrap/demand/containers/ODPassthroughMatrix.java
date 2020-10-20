package edu.utexas.wrap.demand.containers;


import java.util.Collection;

import edu.utexas.wrap.TimePeriod;
import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.ModalPAMatrix;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.TravelSurveyZone;

public class ODPassthroughMatrix implements ODMatrix {
	
	private ModalPAMatrix base;
	private Float vot = null;
	private TimePeriod tp;
	
	public ODPassthroughMatrix(ModalPAMatrix baseMatrix) {
		base = baseMatrix;
	}
	
	public ODPassthroughMatrix(ModalPAMatrix baseMatrix, float vot) {
		this(baseMatrix);
		this.vot = vot;
	}

	@Override
	public Mode getMode() {
		return base.getMode();
	}

	@Override
	public float getDemand(TravelSurveyZone origin, TravelSurveyZone destination) {
		return base.getDemand(origin, destination);
	}

	@Override
	public void put(TravelSurveyZone origin, TravelSurveyZone destination, Float demand) {
		base.put(origin, destination, demand);
	}

	@Override
	public Collection<TravelSurveyZone> getZones() {
		return base.getZones();
	}

	@Override
	public Float getVOT() {
		return vot;
	}

	@Override
	public void setVOT(float VOT) {
		vot = VOT;
	}


	@Override
	public DemandMap getDemandMap(TravelSurveyZone origin) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TimePeriod timePeriod() {
		// TODO Auto-generated method stub
		return tp;
	}

}
