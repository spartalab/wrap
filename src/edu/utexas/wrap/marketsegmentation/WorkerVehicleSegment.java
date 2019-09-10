package edu.utexas.wrap.marketsegmentation;

import java.util.function.ToDoubleFunction;

import edu.utexas.wrap.net.TravelSurveyZone;

public class WorkerVehicleSegment implements WorkerSegmenter, VehicleSegmenter {
	int numWorkers, numVehicles;
	
	public WorkerVehicleSegment(int workers, int vehicles) {
		numWorkers = workers;
		numVehicles = vehicles;
	}

	@Override
	public ToDoubleFunction<TravelSurveyZone> attributeDataGetter() {
		return tsz -> tsz.getHouseholdsByWorkersAndVehicles(numWorkers,numVehicles);
	}

	@Override
	public int getNumberOfWorkers() {
		return numWorkers;
	}
	
	@Override
	public int getNumberOfVehicles() {
		return numVehicles;
	}
	
	public String toString() {
		return "Households with "+numWorkers+" workers and "+numVehicles+" vehicles";
	}
}