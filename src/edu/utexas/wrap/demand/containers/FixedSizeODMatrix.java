package edu.utexas.wrap.demand.containers;

import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;

public class FixedSizeODMatrix implements ODMatrix {
	
	private float vot;
	private final Mode mode;
	private final Graph graph;
	private final FixedSizeDemandMap[] demandMaps;

	public FixedSizeODMatrix(ODMatrix od, Float vot, Mode mode) {
		// TODO Auto-generated constructor stub
		this.mode = mode;
		this.vot = vot;
		this.graph = od.getGraph();
		demandMaps = new FixedSizeDemandMap[graph.numZones()];
		
		graph.getTSZs().forEach(origin -> {
			FixedSizeDemandMap dm = new FixedSizeDemandMap(graph);
			
			graph.getTSZs().forEach(destination -> dm.put(destination, od.getDemand(origin, destination)));
			
			demandMaps[origin.getOrder()] = dm;
		});
		
	}

	@Override
	public Mode getMode() {
		return mode;
	}

	@Override
	public float getDemand(TravelSurveyZone origin, TravelSurveyZone destination) {
		return 	demandMaps[origin.getOrder()].get(destination);
	}

	@Override
	public void put(TravelSurveyZone origin, TravelSurveyZone destination, Float demand) {
		demandMaps[origin.getOrder()].put(destination, demand);
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
		this.vot = VOT;
	}

}
