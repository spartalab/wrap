package edu.utexas.wrap.marketsegmentation;

import java.util.function.ToDoubleFunction;

import edu.utexas.wrap.net.TravelSurveyZone;

public class IncomeGroupWorkerHouseholdSizeSegment
		implements IncomeGroupSegmenter, WorkerSegmenter, HouseholdSizeSegmenter {
	
	private int incomeGrp, workers, hhSize, hash;
	
	public IncomeGroupWorkerHouseholdSizeSegment(int incomeGroup, int numWorkers, int householdSize) {
		incomeGrp = incomeGroup;
		workers = numWorkers;
		hhSize = householdSize;
	}
	
	public IncomeGroupWorkerHouseholdSizeSegment(String arguments) {
		String[] args = arguments.split(",");
		if (args.length != 3) {
			throw new IllegalArgumentException("Mismatch number of arguments. Expected 3, got "+args.length);
		}
		incomeGrp = Integer.parseInt(args[0]);
		workers = Integer.parseInt(args[1]);
		hhSize = Integer.parseInt(args[2]);
	}

	@Override
	public ToDoubleFunction<TravelSurveyZone> attributeDataGetter() {
		// TODO Auto-generated method stub
		return tsz -> tsz.getHouseholdsByIncomeGroupThenWorkersThenSize(incomeGrp,workers,hhSize);
	}

	@Override
	public int getHouseholdSize() {
		// TODO Auto-generated method stub
		return hhSize;
	}

	@Override
	public int getNumberOfWorkers() {
		// TODO Auto-generated method stub
		return workers;
	}

	@Override
	public int getIncomeGroup() {
		// TODO Auto-generated method stub
		return incomeGrp;
	}
	
	public String toString() {
		return "MS:Households of size "+hhSize+" in income group "+incomeGrp+" with "+workers+" workers";
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
			IncomeGroupWorkerHouseholdSizeSegment other = (IncomeGroupWorkerHouseholdSizeSegment) obj;
			return other.incomeGrp == incomeGrp && other.hhSize == hhSize && other.workers == workers;
		} catch (ClassCastException e) {
			return false;
		}
	}
}
