package edu.utexas.wrap.demand.containers;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import edu.utexas.wrap.demand.AutoDemandMap;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;

public class EmptyDemandMap implements AutoDemandMap {
	private Graph g;
	public EmptyDemandMap(Graph gPrime) {
		// TODO Auto-generated constructor stub
		g = gPrime;
	}

	@Override
	public Double get(TravelSurveyZone dest) {
		// TODO Auto-generated method stub
		return 0.0;
	}

	@Override
	public Graph getGraph() {
		// TODO Auto-generated method stub
		return g;
	}

	@Override
	public Collection<TravelSurveyZone> getZones() {
		// TODO Auto-generated method stub
		return new HashSet<TravelSurveyZone>(0,1.0f);
	}

	@Override
	public Double getOrDefault(TravelSurveyZone node, Double f) {
		// TODO Auto-generated method stub
		return 0.0;
	}

	@Override
	public Double put(TravelSurveyZone dest, Double demand) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Unable to add demand to an empty demand map");
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public Map<TravelSurveyZone, Double> doubleClone() {
		// TODO Auto-generated method stub
		return new HashMap<TravelSurveyZone,Double>();
	}

	@Override
	public Float getVOT() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Mode getMode() {
		// TODO Auto-generated method stub
		return null;
	}

}
