package edu.utexas.wrap.demand.containers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;

public class FixedSizeDemandMap implements DemandMap {
	private final Graph graph;
	private double[] demand;
	
	public FixedSizeDemandMap(Graph g) {
		graph = g;
		demand = new double[g.numZones()];
	}
	
	public FixedSizeDemandMap(DemandMap base) {
		graph = base.getGraph();
		demand = new double[graph.numZones()];
		base.getZones().parallelStream().forEach(tsz -> demand[tsz.getOrder()] = base.get(tsz));
	}

	@Override
	public Double get(TravelSurveyZone dest) {
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
	public Double getOrDefault(TravelSurveyZone node, Double f) {
		// TODO Auto-generated method stub
		int index = node.getOrder();
		if (index == -1) 
			throw new RuntimeException();
		return demand[index];
	}

	@Override
	public Double put(TravelSurveyZone dest, Double put) {
		// TODO Auto-generated method stub
		int idx = dest.getOrder();
		Double d = demand[idx];
		demand[idx] = put;
		return d;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		for (Double d : demand) if (d > 0) return false;
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
