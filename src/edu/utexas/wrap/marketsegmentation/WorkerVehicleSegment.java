package edu.utexas.wrap.marketsegmentation;

import java.util.function.ToDoubleFunction;

import edu.utexas.wrap.net.TravelSurveyZone;

public class WorkerVehicleSegment extends MarketSegment {
	int numWorkers, numVehicles;
	
	public WorkerVehicleSegment(int workers, int vehicles, Double segmentRate) {
		super(segmentRate);
		numWorkers = workers;
		numVehicles = vehicles;
	}

	@Override
	public ToDoubleFunction<TravelSurveyZone> getAttributeData() {
		return tsz -> tsz.getHouseholdsByWorkersAndVehicles(numWorkers,numVehicles);
	}
}