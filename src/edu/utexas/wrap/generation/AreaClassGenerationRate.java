package edu.utexas.wrap.generation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import edu.utexas.wrap.net.AreaClass;
import edu.utexas.wrap.net.TravelSurveyZone;

public class AreaClassGenerationRate implements GenerationRate {

	private Map<AreaClass,Double> rates;
	
	public AreaClassGenerationRate() {
		rates = new ConcurrentHashMap<AreaClass,Double>();
	}
	
	@Override
	public double getRate(TravelSurveyZone zone) {
		return rates.getOrDefault(zone.getAreaClass(),0.0);
	}
	
	public Double put(AreaClass ac, Double rate) {
		return rates.put(ac, rate);
	}

}
