package edu.utexas.wrap.marketsegmentation;

import java.util.function.ToDoubleFunction;

import edu.utexas.wrap.net.TravelSurveyZone;

public class WorkerHouseholdSizeSegment extends MarketSegment {
	int numWorkers, householdSize;
	
	public WorkerHouseholdSizeSegment(int workers, int hhSize, Double segmentRate) {
		super(segmentRate);
		numWorkers = workers;
		householdSize = hhSize;
	}

	@Override
	public ToDoubleFunction<TravelSurveyZone> getAttributeData() {
		return tsz -> tsz.getHouseholdsByWorkersAndSize(numWorkers,householdSize);
	}
}