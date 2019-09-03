package edu.utexas.wrap.marketsegmentation;

import java.util.function.ToDoubleFunction;

import edu.utexas.wrap.net.TravelSurveyZone;

public class WorkerSegment implements WorkerSegmenter{
	int numWorkers;
	
	public WorkerSegment(int workers) {
		numWorkers = workers;
	}

	@Override
	public ToDoubleFunction<TravelSurveyZone> attributeDataGetter() {
		return tsz -> tsz.getHouseholdsByWorkers(numWorkers);
	}

	@Override
	public int getNumberOfWorkers() {
		// TODO Auto-generated method stub
		return numWorkers;
	}
}