package edu.utexas.wrap.demand.containers;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;

public class PerProductionZoneMultiplierPassthroughMatrix implements AggregatePAMatrix {

	AggregatePAMatrix parent;
	Map<TravelSurveyZone,Double> rates;
	
	public PerProductionZoneMultiplierPassthroughMatrix(AggregatePAMatrix initialMatrix, Map<TravelSurveyZone, Double> perZoneRates) {
		parent = initialMatrix;
		rates = perZoneRates;
	}

	@Override
	public Graph getGraph() {
		return parent.getGraph();
	}

	@Override
	public void put(TravelSurveyZone producer, TravelSurveyZone attractor, Float demand) {
		// TODO Auto-generated method stub
		throw new RuntimeException();
	}

	@Override
	public Float getDemand(TravelSurveyZone producer, TravelSurveyZone attractor) {
		return (float) (rates.get(producer)*parent.getDemand(producer,attractor));
	}

	@Override
	public DemandMap getDemandMap(TravelSurveyZone producer) {
		return new FixedMultiplierPassthroughDemandMap(parent.getDemandMap(producer),rates.get(producer).floatValue());
	}

	@Override
	public Collection<TravelSurveyZone> getProducers() {
		return parent.getProducers().parallelStream().filter(zone -> rates.get(zone)>0).collect(Collectors.toSet());
	}

}
