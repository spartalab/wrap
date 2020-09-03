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

	public Collection<TravelSurveyZone> getProducers() {
		return prods.getZones();
	}

	public Collection<TravelSurveyZone> getAttractors() {
		return attrs.getZones();
	}

	public float getAttractions(TravelSurveyZone z) {
		return attrs.get(z);
	}

	public float getProductions(TravelSurveyZone z) {
		return prods.get(z);
	}

	public void putAttractions(TravelSurveyZone z, Float amt) {
		attrs.put(z, amt);
	}

	public void putProductions(TravelSurveyZone z, Float amt) {
		prods.put(z, amt);
	}

	public DemandMap getProductionMap() {
		return prods;
	}

	public DemandMap getAttractionMap() {
		return attrs;
	}

}
