package edu.utexas.wrap.demand.containers;

import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;

public class TransposeODMatrix implements ODMatrix {

	ODMatrix parent;
	
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
	public Graph getGraph() {
		return parent.getGraph();
	}

	@Override
	public Float getVOT() {
		return parent.getVOT();
	}

	@Override
	public void setVOT(float VOT) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Read-only matrix");
	}

}
