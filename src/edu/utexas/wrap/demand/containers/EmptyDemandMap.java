package edu.utexas.wrap.demand.containers;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import edu.utexas.wrap.demand.AutoDemandMap;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.TravelSurveyZone;

public class EmptyDemandMap implements AutoDemandMap {
	
	public EmptyDemandMap() {
	}

	@Override
	public float get(TravelSurveyZone dest) {
		return 0.0f;
	}

	@Override
	public Collection<TravelSurveyZone> getZones() {
		return Collections.<TravelSurveyZone>emptySet();
	}

	@Override
	public Float put(TravelSurveyZone dest, Float demand) {
		throw new RuntimeException("Unable to add demand to an empty demand map");
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public Map<TravelSurveyZone, Double> doubleClone() {
		return Collections.<TravelSurveyZone,Double>emptyMap();
	}

	@Override
	public Float getVOT() {
		return null;
	}

	@Override
	public Mode getMode() {
		return null;
	}

}
