package edu.utexas.wrap.marketsegmentation;

import java.util.function.ToDoubleFunction;

import edu.utexas.wrap.net.TravelSurveyZone;

public class WorkerVehicleSegment implements WorkerSegmenter, VehicleSegmenter {
	private int numWorkers, numVehicles, hash;
	
	public WorkerVehicleSegment(int workers, int vehicles) {
		numWorkers = workers;
		numVehicles = vehicles;
	}

	public WorkerVehicleSegment(String arguments) {
		String[] args = arguments.split(",");
		if(args.length != 2) {
			throw new IllegalArgumentException("Mismatch number of arguments expected 2 got " + args.length);
		}
		this.numWorkers = Integer.parseInt(args[0]);
		this.numVehicles = Integer.parseInt(args[1]);
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

	@Override
	public int hashCode() {
		if(hash == 0) {
			hash = toString().hashCode();
		}
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		try {
			WorkerVehicleSegment other = (WorkerVehicleSegment) obj;
			return other.numWorkers == numWorkers && other.numVehicles == numVehicles;
		} catch (ClassCastException e) {
			return false;
		}
	}
}