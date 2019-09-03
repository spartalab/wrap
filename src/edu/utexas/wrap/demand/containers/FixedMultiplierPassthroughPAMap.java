package edu.utexas.wrap.demand.containers;

import java.util.Set;

import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;

public class FixedMultiplierPassthroughPAMap implements PAMap {
	private PAMap base;
	private double rate;

	public FixedMultiplierPassthroughPAMap(PAMap baseMap, double rate) {
		// TODO Auto-generated constructor stub
		base = baseMap;
		this.rate = rate;
	}

	@Override
	public Set<TravelSurveyZone> getProducers() {
		return base.getProducers();
	}

	@Override
	public Set<TravelSurveyZone> getAttractors() {
		return base.getAttractors();
	}

	@Override
	public Float getAttractions(TravelSurveyZone z) {
		return (float) (rate*base.getAttractions(z));
	}

	@Override
	public Float getProductions(TravelSurveyZone z) {
		return (float) rate*base.getProductions(z);
	}

	@Override
	public Graph getGraph() {
		return base.getGraph();
	}

	@Override
	public void putAttractions(TravelSurveyZone z, Float amt) {
		throw new RuntimeException("Writing to read-only matrix");
	}

	@Override
	public void putProductions(TravelSurveyZone z, Float amt) {
		throw new RuntimeException("Writing to read-only matrix");
	}

	@Override
	public Float getVOT() {
		return base.getVOT();
	}

}
