package edu.utexas.wrap.marketsegmentation;

import java.util.function.ToDoubleFunction;

import edu.utexas.wrap.net.TravelSurveyZone;

public class WorkerHouseholdSizeSegment implements WorkerSegmenter, HouseholdSizeSegmenter {
	private int numWorkers, householdSize, hash;
	
	public WorkerHouseholdSizeSegment(int workers, int hhSize) {
		numWorkers = workers;
		householdSize = hhSize;
	}

	public WorkerHouseholdSizeSegment(String arguments) {
		String[] args = arguments.split(",");
		if(args.length != 2) {
			throw new IllegalArgumentException("Mismatch number of arguments expected 2 got " + args.length);
		}
		this.numWorkers = Integer.parseInt(args[0]);
		this.householdSize = Integer.parseInt(args[1]);
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
	
	public String toString() {
		return "MS: Households of size "+householdSize+" with "+numWorkers+" workers";
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
			WorkerHouseholdSizeSegment other = (WorkerHouseholdSizeSegment) obj;
			return other.numWorkers == numWorkers && other.householdSize == householdSize;
		} catch (ClassCastException e) {
			return false;
		}
	}
}