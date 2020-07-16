package edu.utexas.wrap.demand.containers;

import java.util.Collection;

import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.net.TravelSurveyZone;

public class FixedMultiplierPassthroughPAMap implements PAMap {
	private PAMap base;
	private double rate;

	public FixedMultiplierPassthroughPAMap(PAMap baseMap, double rate) {
		base = baseMap;
		this.rate = rate;
	}

	@Override
	public Collection<TravelSurveyZone> getProducers() {
		return base.getProducers();
	}

	@Override
	public Collection<TravelSurveyZone> getAttractors() {
		return base.getAttractors();
	}

	@Override
	public float getAttractions(TravelSurveyZone z) {
		return (float) (rate*base.getAttractions(z));
	}

	@Override
	public float getProductions(TravelSurveyZone z) {
		return (float) rate*base.getProductions(z);
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
	public DemandMap getProductionMap() {
		// TODO Auto-generated method stub
		throw new RuntimeException();
	}

	@Override
	public DemandMap getAttractionMap() {
		// TODO Auto-generated method stub
		throw new RuntimeException();
	}

}
