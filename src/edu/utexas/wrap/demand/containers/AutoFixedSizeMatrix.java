package edu.utexas.wrap.demand.containers;

import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;

public class AutoFixedSizeMatrix implements ODMatrix {

	private Graph graph;
	private Mode mode;
	private DemandMap[] origins;
	private float vot;
	private String modvot;
	
	public AutoFixedSizeMatrix(Graph g, Mode mode, Float vot) {
		graph = g;
		this.mode = mode;
		this.vot = vot;
		modvot = getMode() + "_" + getVOT();
		origins = new DemandMap[g.numZones()];
	}
	
	@Override
	public Mode getMode() {
		return mode;
	}

	@Override
	public Float getDemand(TravelSurveyZone origin, TravelSurveyZone destination) {
		DemandMap orig = origins[origin.getOrder()];
		return orig == null? 0.0f : orig.get(destination).floatValue();
	}

	@Override
	public void put(TravelSurveyZone origin, TravelSurveyZone destination, Float demand) {
		if (origins[origin.getOrder()] == null) origins[origin.getOrder()] = new FixedSizeDemandMap(graph);
		origins[origin.getOrder()].put(destination, demand.doubleValue());
	}

	@Override
	public Graph getGraph() {
		return graph;
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
		return modvot;
	}

}
