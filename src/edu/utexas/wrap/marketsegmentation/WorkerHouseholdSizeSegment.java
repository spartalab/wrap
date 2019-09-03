package edu.utexas.wrap.marketsegmentation;

import java.util.function.ToDoubleFunction;

import edu.utexas.wrap.net.TravelSurveyZone;

public class WorkerHouseholdSizeSegment implements WorkerSegmenter, HouseholdSizeSegmenter {
	int numWorkers, householdSize;
	
	public WorkerHouseholdSizeSegment(int workers, int hhSize) {
		numWorkers = workers;
		householdSize = hhSize;
	}

	@Override
	public ToDoubleFunction<TravelSurveyZone> attributeDataGetter() {
		return tsz -> tsz.getHouseholdsByWorkersAndSize(numWorkers,householdSize);
	}

	@Override
	public int getNumberOfWorkers() {
		return numWorkers;
	}

	@Override
	public int getHouseholdSize() {
		return householdSize;
	}
}