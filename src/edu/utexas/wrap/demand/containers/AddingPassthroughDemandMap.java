package edu.utexas.wrap.demand.containers;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.net.TravelSurveyZone;

public class AddingPassthroughDemandMap implements DemandMap {
	private final DemandMap demand1, demand2;
	
	public AddingPassthroughDemandMap(DemandMap dm1, DemandMap dm2) {
		demand1 = dm1;
		demand2 = dm2;
	}

	@Override
	public float get(TravelSurveyZone dest) {
		// TODO Auto-generated method stub
		return demand1.get(dest)+demand2.get(dest);
	}

	@Override
	public Collection<TravelSurveyZone> getZones() {
		// TODO Auto-generated method stub
		Set<TravelSurveyZone> set = new HashSet<TravelSurveyZone>(demand1.getZones());
		set.addAll(demand2.getZones());
		return set;
	}

	@Override
	public Float put(TravelSurveyZone dest, Float demand) {
		// TODO Auto-generated method stub
		throw new RuntimeException();
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return demand1.isEmpty() && demand2.isEmpty();
	}

	@Override
	public Map<TravelSurveyZone, Double> doubleClone() {
		// TODO Auto-generated method stub
		return null;
	}

}
