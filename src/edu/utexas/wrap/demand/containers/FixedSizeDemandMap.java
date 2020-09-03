package edu.utexas.wrap.demand.containers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.net.TravelSurveyZone;

public class FixedSizeDemandMap implements DemandMap {
//	private final Graph graph;
	private final Collection<TravelSurveyZone> zones;
	private final float[] demand;
	
	public FixedSizeDemandMap(Collection<TravelSurveyZone> zones) {
		this.zones = zones;
		demand = new float[zones.size()];
	}
	
	public FixedSizeDemandMap(DemandMap base) {
		zones = base.getZones();
		demand = new float[zones.size()];
		base.getZones().parallelStream().forEach(tsz -> demand[tsz.getOrder()] = base.get(tsz));
	}
	
	public FixedSizeDemandMap(Stream<DemandMap> baseMapStream) {
		Collection<DemandMap> baseMaps = baseMapStream.collect(Collectors.toSet());
		zones = baseMaps.stream().findFirst().get().getZones();
		demand = new float[zones.size()];
		zones.forEach(tsz -> demand[tsz.getOrder()] = (float)
				baseMaps.parallelStream()
				.mapToDouble(dm -> (double) dm.get(tsz))
				.sum()
			);
		
	}

	public float get(TravelSurveyZone dest) {
		try{
			return demand[dest.getOrder()];
		} catch (NullPointerException e) {
			return 0.0f;
		}
	}

	public Collection<TravelSurveyZone> getZones() {
		return zones;
	}

	public Float put(TravelSurveyZone dest, Float put) {
		int idx = dest.getOrder();
		Float d = demand[idx];
		demand[idx] = put;
		return d;
	}

	public boolean isEmpty() {
		for (Float d : demand) if (d > 0) return false;
		return true;
	}

	public Map<TravelSurveyZone, Double> doubleClone() {
		Map<TravelSurveyZone, Double> ret = new HashMap<TravelSurveyZone, Double>();
		for (TravelSurveyZone n : zones) {
			ret.put(n,(double) demand[n.getOrder()]);
		}
		return ret;
	}

}
