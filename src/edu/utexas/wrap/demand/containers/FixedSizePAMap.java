package edu.utexas.wrap.demand.containers;

import java.util.Collection;
import java.util.stream.Stream;

import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.net.TravelSurveyZone;

public class FixedSizePAMap implements PAMap {
	final DemandMap prods, attrs;
	
	public FixedSizePAMap(PAMap base) {
		prods = new FixedSizeDemandMap(base.getProductionMap());
		attrs = new FixedSizeDemandMap(base.getAttractionMap());
	}
	
	public FixedSizePAMap(Stream<DemandMap> prodMapStream, Stream<DemandMap> attrMapStream) {
		prods = new FixedSizeDemandMap(prodMapStream);
		attrs = new FixedSizeDemandMap(attrMapStream);
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
	public float getAttractions(TravelSurveyZone z) {
		return attrs.get(z);
	}

	@Override
	public float getProductions(TravelSurveyZone z) {
		return prods.get(z);
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
