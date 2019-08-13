package edu.utexas.wrap.marketsegmentation;

import java.util.function.ToDoubleFunction;

import edu.utexas.wrap.net.TravelSurveyZone;

public class IncomeGroupSegment extends MarketSegment {
	int incomeGroup;
	
	public IncomeGroupSegment(Integer incomeGrp, Double rate) {
		super(rate);
		incomeGroup = incomeGrp;
	}
	
	@Override
	public ToDoubleFunction<TravelSurveyZone> getAttributeData() {
		return tsz -> tsz.getHouseholdsByIncomeGroup(incomeGroup);
	}
}