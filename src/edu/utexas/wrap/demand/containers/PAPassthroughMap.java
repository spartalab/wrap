package edu.utexas.wrap.demand.containers;

import java.util.Map;
import java.util.Set;

import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;

public class PAPassthroughMap implements PAMap {
	private Graph g;
	private Map<TravelSurveyZone,Double> prods, attrs;
	private Double vot;
	
	public PAPassthroughMap(Graph g, Double valueOfTime, Map<TravelSurveyZone,Double> productions, Map<TravelSurveyZone,Double> attractions) {
		this.g = g;
		vot = valueOfTime;
		prods = productions;
		attrs = attractions;
	}

	@Override
	public Set<TravelSurveyZone> getProducers() {
		return prods.keySet();
	}

	@Override
	public Set<TravelSurveyZone> getAttractors() {
		return attrs.keySet();
	}

	@Override
	public Float getAttractions(TravelSurveyZone z) {
		return attrs.get(z).floatValue();
	}

	@Override
	public Float getProductions(TravelSurveyZone z) {
		return prods.get(z).floatValue();
	}

	@Override
	public Graph getGraph() {
		return g;
	}

	@Override
	public void putAttractions(TravelSurveyZone z, Float amt) {
		throw new RuntimeException("Writing to a read-only map");
	}

	@Override
	public void putProductions(TravelSurveyZone z, Float amt) {
		throw new RuntimeException("Writing to a read-only map");
	}

	@Override
	public Float getVOT() {
		return vot.floatValue();
	}

}
