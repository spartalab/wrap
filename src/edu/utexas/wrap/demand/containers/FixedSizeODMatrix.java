package edu.utexas.wrap.demand.containers;

import java.util.Collection;
import java.util.stream.Collectors;

import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;

public class FixedSizeODMatrix<T extends DemandMap> implements ODMatrix {
	
	private float vot;
	private final Mode mode;
	private final Graph graph;
	private final DemandMap[] demandMaps;
	
	public FixedSizeODMatrix(Float vot, Mode mode, Graph graph) {
		this.mode = mode;
		this.vot = vot;
		this.graph = graph;
		this.demandMaps = new DemandMap[graph.numZones()];
	}

	//Matrix multiplier constructor
	public FixedSizeODMatrix(Float vot, Mode mode, ODMatrix od, float multiplier) {
		this(vot,mode, od.getGraph());
		
		graph.getTSZs().forEach(origin -> {
			FixedSizeDemandMap dm = new FixedSizeDemandMap(graph);
			
			graph.getTSZs().forEach(destination -> dm.put(destination, multiplier*od.getDemand(origin, destination)));
			
			demandMaps[origin.getOrder()] = dm;
		});
		
	}
	
	public FixedSizeODMatrix(Float vot, Mode mode, ODMatrix od) {
		this(vot,mode,od,1.0f);
	}
	
	//Matrix adder constructor
	public FixedSizeODMatrix(Float vot, Mode mode, ODMatrix od1, ODMatrix od2) {
		this(vot, mode, od1.getGraph());
		
		graph.getTSZs().forEach(origin -> {
			FixedSizeDemandMap dm = new FixedSizeDemandMap(graph);
			
			graph.getTSZs().forEach(destination -> dm.put(destination, od1.getDemand(origin, destination) + od2.getDemand(origin, destination)));
			
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

	@Override
	public Collection<TravelSurveyZone> getOrigins() {
		return graph.getTSZs().parallelStream()
				.filter(zone -> demandMaps[zone.getOrder()] != null)
				.collect(Collectors.toSet());
	}

	@Override
	public DemandMap getDemandMap(TravelSurveyZone origin) {
		return demandMaps[origin.getOrder()];
	}
	
	public void setDemandMap(TravelSurveyZone origin, T demandMap) {
		demandMaps[origin.getOrder()] = demandMap;
	}

	
	public String toString() {
		return mode + "_" + getVOT();
	}
}
