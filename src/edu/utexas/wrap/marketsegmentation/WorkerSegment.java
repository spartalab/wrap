package edu.utexas.wrap.marketsegmentation;

import java.util.function.ToDoubleFunction;

import edu.utexas.wrap.net.TravelSurveyZone;

public class WorkerSegment implements WorkerSegmenter{
	private int numWorkers, hash;
	
	public WorkerSegment(int workers) {
		numWorkers = workers;
	}

	public WorkerSegment(String arguments) {
		String[] args = arguments.split(",");
		if(args.length != 1) {
			throw new IllegalArgumentException("Mismatch number of arguments expected 1 got " + args.length);
		}
		this.numWorkers = Integer.parseInt(args[0]);
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
	
	public String toString() {
		return "Households with "+numWorkers+" workers";
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
			WorkerSegment other = (WorkerSegment) obj;
			return other.numWorkers == numWorkers;
		} catch (ClassCastException e) {
			return false;
		}
	}
}