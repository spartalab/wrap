package edu.utexas.wrap.marketsegmentation;

import java.util.function.ToDoubleFunction;

import edu.utexas.wrap.net.TravelSurveyZone;

public class WorkerSegment extends MarketSegment {
	int numWorkers;
	
	public WorkerSegment(int workers, Double segmentRate) {
		super(segmentRate);
		numWorkers = workers;
	}

	@Override
	public ToDoubleFunction<TravelSurveyZone> getAttributeData() {
		return tsz -> tsz.getHouseholdsByWorkers(numWorkers);
	}
}