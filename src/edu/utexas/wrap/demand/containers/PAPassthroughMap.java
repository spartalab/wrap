package edu.utexas.wrap.demand.containers;

import java.util.Collection;
import java.util.Collections;

import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;

public class PAPassthroughMap implements PAMap {
	private Graph g;
	private DemandMap prods, attrs;
	
	public PAPassthroughMap(Graph g, DemandMap productions, DemandMap attractions) {
		this.g = g;
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
	public Float getAttractions(TravelSurveyZone z) {
		return attrs == null? 0.0f : attrs.get(z).floatValue();
	}

	@Override
	public Float getProductions(TravelSurveyZone z) {
		return prods == null? 0.0f : prods.get(z).floatValue();
	}

	@Override
	public Graph getGraph() {
		return g;
	}

	@Override
	public void putAttractions(TravelSurveyZone z, Float amt) {
		attrs.put(z, amt.doubleValue());
	}

	@Override
	public void putProductions(TravelSurveyZone z, Float amt) {
		prods.put(z, amt.doubleValue());
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
