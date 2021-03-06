package edu.utexas.wrap.demand.containers;

import java.util.Collection;
import java.util.Map;

import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.net.TravelSurveyZone;

public class FixedMultiplierPassthroughDemandMap implements DemandMap {
	private DemandMap parent;
	private float multiplier;

	public FixedMultiplierPassthroughDemandMap(DemandMap demandMap, float percent) {
		parent = demandMap;
		this.multiplier = percent;
	}

	@Override
	public float get(TravelSurveyZone dest) {
		return multiplier*parent.get(dest);
	}

	@Override
	public Collection<TravelSurveyZone> getZones() {
		return parent.getZones();
	}

	@Override
	public Float put(TravelSurveyZone dest, Float demand) {
		return parent.put(dest, demand/multiplier);
	}

	@Override
	public boolean isEmpty() {
		return parent.isEmpty();
	}

	@Override
	public Map<TravelSurveyZone, Double> doubleClone() {
		// TODO Auto-generated method stub
		throw new RuntimeException();
	}

}
