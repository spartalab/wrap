package edu.utexas.wrap.net;

import java.util.stream.IntStream;

import edu.utexas.wrap.demand.DemandMap;

public class SecondaryDemographic implements Demographic {
	
	private final DemandMap[] components;
	
	public SecondaryDemographic(DemandMap[] components) {
		this.components = components;
	}

	@Override
	public Float[] valueFor(TravelSurveyZone zone) {
		return IntStream.range(0, components.length)
				.mapToObj(index -> (float) components[index].get(zone))
				.toArray(Float[]::new);
	}
}
