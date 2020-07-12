package edu.utexas.wrap.demand.containers;

import java.util.Collection;
import java.util.Collections;

import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;

public class PAPassthroughMap implements PAMap {
	private DemandMap prods, attrs;
	
	public PAPassthroughMap(DemandMap productions, DemandMap attractions) {
		prods = productions;
		attrs = attractions;
	}

	@Override
	public Collection<TravelSurveyZone> getProducers() {
		return prods == null? Collections.emptySet() : prods.getZones();
	}

	@Override
	public Collection<TravelSurveyZone> getAttractors() {
		return attrs == null? Collections.emptySet() : attrs.getZones();
	}

	@Override
	public float getAttractions(TravelSurveyZone z) {
		return attrs == null? 0.0f : attrs.get(z);
	}

	@Override
	public float getProductions(TravelSurveyZone z) {
		return prods == null? 0.0f : prods.get(z);
	}

	@Override
	public Graph getGraph() {
		return prods.getGraph();
	}

	@Override
	public void putAttractions(TravelSurveyZone z, Float amt) {
		attrs.put(z, amt);
	}

	@Override
	public void putProductions(TravelSurveyZone z, Float amt) {
		prods.put(z, amt);
	}

	@Override
	public DemandMap getProductionMap() {
		return prods;
	}

	@Override
	public DemandMap getAttractionMap() {
		return attrs;
	}

}
