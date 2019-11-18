package edu.utexas.wrap.demand.containers;

import java.util.Collection;

import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;

public class FixedSizePAMap implements PAMap {
	Graph graph;
	DemandMap prods, attrs;
	
	public FixedSizePAMap(PAMap base) {
		graph = base.getGraph();
		prods = new FixedSizeDemandMap(base.getProductionMap());
		attrs = new FixedSizeDemandMap(base.getAttractionMap());
	}

	@Override
	public Collection<TravelSurveyZone> getProducers() {
		// TODO Auto-generated method stub
		return prods.getZones();
	}

	@Override
	public Collection<TravelSurveyZone> getAttractors() {
		// TODO Auto-generated method stub
		return attrs.getZones();
	}

	@Override
	public Float getAttractions(TravelSurveyZone z) {
		// TODO Auto-generated method stub
		return attrs.get(z).floatValue();
	}

	@Override
	public Float getProductions(TravelSurveyZone z) {
		// TODO Auto-generated method stub
		return prods.get(z).floatValue();
	}

	@Override
	public Graph getGraph() {
		// TODO Auto-generated method stub
		return graph;
	}

	@Override
	public void putAttractions(TravelSurveyZone z, Float amt) {
		// TODO Auto-generated method stub
		attrs.put(z, amt.doubleValue());
	}

	@Override
	public void putProductions(TravelSurveyZone z, Float amt) {
		// TODO Auto-generated method stub
		prods.put(z, amt.doubleValue());
	}

	@Override
	public DemandMap getProductionMap() {
		// TODO Auto-generated method stub
		return prods;
	}

	@Override
	public DemandMap getAttractionMap() {
		// TODO Auto-generated method stub
		return attrs;
	}

}
