package edu.utexas.wrap.net;

import java.util.stream.IntStream;

import edu.utexas.wrap.demand.DemandMap;

/**A demographic whose components are a series of DemandMaps
 * 
 * For trips which depend on the amount of other trips taken,
 * this class provides a mechanism for using the number of trips
 * from a given purpose (can be unbalanced or balanced) as the 
 * base demographic for another trip purpose's generation step
 * 
 * @author William
 *
 */
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
