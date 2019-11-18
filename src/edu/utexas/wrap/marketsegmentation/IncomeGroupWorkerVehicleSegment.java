package edu.utexas.wrap.marketsegmentation;

import java.util.function.ToDoubleFunction;

import edu.utexas.wrap.net.TravelSurveyZone;

public class IncomeGroupWorkerVehicleSegment implements IncomeGroupSegmenter, VehicleSegmenter, WorkerSegmenter, MarketSegment {

	private int incomeGroup, workerCount, vehicleCount, hash;
	public IncomeGroupWorkerVehicleSegment(int incomeGrp, int workerCnt, int vehCnt) {
		incomeGroup = incomeGrp;
		workerCount = workerCnt;
		vehicleCount = vehCnt;
	}

	public IncomeGroupWorkerVehicleSegment(String arguments) {
		String[] args = arguments.split(",");
		if(args.length != 3) {
			throw new IllegalArgumentException("Mismatch number of arguments expected 3 got " + args.length);
		}
		this.incomeGroup = Integer.parseInt(args[0]);
		this.workerCount = Integer.parseInt(args[1]);
		this.vehicleCount = Integer.parseInt(args[2]);
	}
	
	@Override
	public ToDoubleFunction<TravelSurveyZone> attributeDataGetter() {
		// TODO Auto-generated method stub
		return tsz -> {
			Double val = tsz.getHouseholdsByIncomeGroupThenWorkersThenVehicles(incomeGroup,workerCount,vehicleCount);
			if (val == null) {
				System.out.println(tsz.getNode().getID());
			}
			return val;
		};
	}

	@Override
	public int getNumberOfWorkers() {
		return workerCount;
	}

	@Override
	public int getNumberOfVehicles() {
		return vehicleCount;
	}

	@Override
	public int getIncomeGroup() {
		return incomeGroup;
	}
	
	public String toString() {
		return "MS:Households in income group "+incomeGroup+" with "+workerCount+" workers and "+vehicleCount+" vehicles";
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
			IncomeGroupWorkerVehicleSegment other = (IncomeGroupWorkerVehicleSegment) obj;
			return other.incomeGroup == incomeGroup && other.vehicleCount == vehicleCount && other.workerCount == workerCount;
		} catch (ClassCastException e) {
			return false;
		}
	}
}
