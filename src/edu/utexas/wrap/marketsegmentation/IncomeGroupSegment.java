package edu.utexas.wrap.marketsegmentation;

import java.util.function.ToDoubleFunction;

import edu.utexas.wrap.net.TravelSurveyZone;

public class IncomeGroupSegment implements IncomeGroupSegmenter {
	int incomeGroup;
	
	public IncomeGroupSegment(Integer incomeGrp) {
		incomeGroup = incomeGrp;
	}
	
	@Override
	public ToDoubleFunction<TravelSurveyZone> attributeDataGetter() {
		return tsz -> tsz.getHouseholdsByIncomeGroup(incomeGroup);
	}

	@Override
	public int getIncomeGroup() {
		return incomeGroup;
	}
}