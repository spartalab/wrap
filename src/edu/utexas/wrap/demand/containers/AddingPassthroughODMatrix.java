package edu.utexas.wrap.demand.containers;

import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.TravelSurveyZone;

public class AddingPassthroughODMatrix implements ODMatrix {

	private final ODMatrix od1, od2;
	
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

}
