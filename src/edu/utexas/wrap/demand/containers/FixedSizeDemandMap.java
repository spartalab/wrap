package edu.utexas.wrap.demand.containers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;

public class FixedSizeDemandMap implements DemandMap {
	private final Graph graph;
	private float[] demand;
	
	public FixedSizeDemandMap(Graph g) {
		graph = g;
		demand = new float[g.numNodes()];
	}

	@Override
	public Float get(TravelSurveyZone dest) {
		// TODO Auto-generated method stub
		return demand[dest.getOrder()];
	}

	@Override
	public Graph getGraph() {
		// TODO Auto-generated method stub
		return graph;
	}

	@Override
	public Collection<TravelSurveyZone> getZones() {
		// TODO Auto-generated method stub
		return graph.getTSZs();
	}

	@Override
	public Float getOrDefault(TravelSurveyZone node, float f) {
		// TODO Auto-generated method stub
		int index = node.getOrder();
		if (index == -1) 
			throw new RuntimeException();
		return demand[index];
	}

	@Override
	public Float put(TravelSurveyZone dest, Float put) {
		// TODO Auto-generated method stub
		int idx = dest.getOrder();
		Float d = demand[idx];
		demand[idx] = put;
		return d;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		for (Float d : demand) if (d > 0) return false;
		return true;
	}

	@Override
	public Map<TravelSurveyZone, Double> doubleClone() {
		// TODO Auto-generated method stub
		Map<TravelSurveyZone, Double> ret = new HashMap<TravelSurveyZone, Double>();
		for (TravelSurveyZone n : graph.getTSZs()) {
			ret.put(n,(double) demand[n.getOrder()]);
		}
		return ret;
	}

}
