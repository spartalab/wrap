package edu.utexas.wrap.generation;

import java.util.Collection;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.containers.FixedSizeDemandMap;
import edu.utexas.wrap.net.Demographic;
import edu.utexas.wrap.net.TravelSurveyZone;

public class ComponentTripGenerator implements TripGenerator {

	private Collection<TravelSurveyZone> zones;
	private GenerationRate[] rates;
	private DemandMap[] components;
	
	public ComponentTripGenerator(Collection<TravelSurveyZone> zones, GenerationRate[] generationRates) {
		this.zones = zones;
		rates = generationRates;
		components = IntStream.range(0, rates.length).mapToObj(i -> new FixedSizeDemandMap(zones)).toArray(DemandMap[]::new);
	}
	
	public DemandMap generate(Demographic demographic){
		
		IntStream.range(0, rates.length).forEach(category -> {
			zones.stream().forEach(tsz -> components[category].put(tsz, (float) rates[category].getRate(tsz) * demographic.valueFor(tsz)[category]));
		});
		
		DemandMap ret = new FixedSizeDemandMap(zones);

		zones.stream()
		.forEach(tsz -> {
			ret.put(tsz, 
					(float) Stream.of(components).mapToDouble(component -> component.get(tsz)).sum());
		});
		
		return ret;
	}
	
	public DemandMap[] getComponents() {
		return components;
	}
	

}

