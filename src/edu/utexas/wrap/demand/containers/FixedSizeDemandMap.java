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
	private float[] demand;
	
	public FixedSizeDemandMap(Graph g) {
		graph = g;
		demand = new float[g.numZones()];
	}
	
	public FixedSizeDemandMap(DemandMap base) {
		graph = base.getGraph();
		demand = new float[graph.numZones()];
		base.getZones().parallelStream().forEach(tsz -> demand[tsz.getOrder()] = base.get(tsz));
	}
	
	public FixedSizeDemandMap(Stream<DemandMap> baseMapStream) {
		Collection<DemandMap> baseMaps = baseMapStream.collect(Collectors.toSet());
		graph = baseMaps.parallelStream().map(DemandMap::getGraph).findAny().get();
		demand = new float[graph.numZones()];
		graph.getTSZs().forEach(tsz -> demand[tsz.getOrder()] = (float)
				baseMaps.parallelStream()
				.mapToDouble(dm -> (double) dm.get(tsz))
				.sum()
			);
		
	}

	@Override
	public float get(TravelSurveyZone dest) {
		try{
			return demand[dest.getOrder()];
		} catch (NullPointerException e) {
			return 0.0f;
		}
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
	public Float put(TravelSurveyZone dest, Float put) {
		int idx = dest.getOrder();
		Float d = demand[idx];
		demand[idx] = put;
		return d;
	}

	@Override
	public boolean isEmpty() {
		for (Float d : demand) if (d > 0) return false;
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
