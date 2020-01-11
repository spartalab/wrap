package edu.utexas.wrap.demand.containers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
	
	public FixedSizeDemandMap(Stream<DemandMap> baseMapStream) {
		Collection<DemandMap> baseMaps = baseMapStream.collect(Collectors.toSet());
		graph = baseMaps.parallelStream().map(DemandMap::getGraph).findAny().get();
		demand = new double[graph.numZones()];
		graph.getTSZs().forEach(tsz -> demand[tsz.getOrder()] = 
				baseMaps.parallelStream()
				.mapToDouble(dm -> dm.get(tsz))
				.sum()
			);
		
	}

	@Override
	public Double get(TravelSurveyZone dest) {
		return demand[dest.getOrder()];
	}

	@Override
	public Graph getGraph() {
		return graph;
	}

	@Override
	public Collection<TravelSurveyZone> getZones() {
		return graph.getTSZs().parallelStream()
				.filter(tsz -> get(tsz) > 0)
				.collect(Collectors.toSet());
	}

	@Override
	public Double getOrDefault(TravelSurveyZone node, Double f) {
		int index = node.getOrder();
		if (index == -1) 
			throw new RuntimeException();
		return demand[index];
	}

	@Override
	public Double put(TravelSurveyZone dest, Double put) {
		int idx = dest.getOrder();
		Double d = demand[idx];
		demand[idx] = put;
		return d;
	}

	@Override
	public boolean isEmpty() {
		for (Double d : demand) if (d > 0) return false;
		return true;
	}

	@Override
	public Map<TravelSurveyZone, Double> doubleClone() {
		Map<TravelSurveyZone, Double> ret = new HashMap<TravelSurveyZone, Double>();
		for (TravelSurveyZone n : graph.getTSZs()) {
			ret.put(n,(double) demand[n.getOrder()]);
		}
		return ret;
	}

}
