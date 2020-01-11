package edu.utexas.wrap.demand.containers;

import java.util.Collection;
import java.util.stream.Stream;

import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;

public class FixedSizePAMap implements PAMap {
	final Graph graph;
	final DemandMap prods, attrs;
	
	public FixedSizePAMap(PAMap base) {
		graph = base.getGraph();
		prods = new FixedSizeDemandMap(base.getProductionMap());
		attrs = new FixedSizeDemandMap(base.getAttractionMap());
	}
	
	public FixedSizePAMap(Stream<DemandMap> prodMapStream, Stream<DemandMap> attrMapStream) {
		prods = new FixedSizeDemandMap(prodMapStream);
		attrs = new FixedSizeDemandMap(attrMapStream);
		graph = prods.getGraph();
	}

	@Override
	public Collection<TravelSurveyZone> getProducers() {
		return prods.getZones();
	}

	@Override
	public Collection<TravelSurveyZone> getAttractors() {
		return attrs.getZones();
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
		return graph;
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
