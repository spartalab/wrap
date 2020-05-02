package edu.utexas.wrap.demand;


import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;

public class ODPassthroughMatrix implements ODMatrix {
	
	private ModalPAMatrix base;
	private Float vot = null;
	private String modvot = null;
	
	public ODPassthroughMatrix(ModalPAMatrix baseMatrix) {
		base = baseMatrix;
	}

	@Override
	public Mode getMode() {
		return base.getMode();
	}

	@Override
	public Float getDemand(TravelSurveyZone origin, TravelSurveyZone destination) {
		return base.getDemand(origin, destination);
	}

	@Override
	public void put(TravelSurveyZone origin, TravelSurveyZone destination, Float demand) {
		base.put(origin, destination, demand);
	}

	@Override
	public Graph getGraph() {
		return base.getGraph();
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
	public String getModeVOTString() {
		if (modvot == null) modvot = base.getMode() + "_" + getVOT();
		return modvot;
	}


}
