package edu.utexas.wrap.marketsegmentation;

import java.util.function.ToDoubleFunction;

import edu.utexas.wrap.net.TravelSurveyZone;

public class IncomeGroupWorkerVehicleSegment implements IncomeGroupSegmenter, VehicleSegmenter, WorkerSegmenter, MarketSegment {

	private int incomeGroup, workerCount, vehicleCount;
	public IncomeGroupWorkerVehicleSegment(int incomeGrp, int workerCnt, int vehCnt) {
		incomeGroup = incomeGrp;
		workerCount = workerCnt;
		vehicleCount = vehCnt;
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

}
