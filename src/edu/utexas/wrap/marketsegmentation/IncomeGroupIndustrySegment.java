package edu.utexas.wrap.marketsegmentation;

import java.util.function.ToDoubleFunction;

import edu.utexas.wrap.generation.IndustryClass;
import edu.utexas.wrap.net.TravelSurveyZone;

public class IncomeGroupIndustrySegment extends MarketSegment {

	int incomeGroup;
	IndustryClass industry;
	
	public IncomeGroupIndustrySegment(Integer incomeGrp, IndustryClass industry, Double segmentRate) {
		super(segmentRate);
		incomeGroup = incomeGrp;
		this.industry = industry;
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public ToDoubleFunction<TravelSurveyZone> getAttributeData() {
		return tsz -> tsz.getEmploymentByIncomeGroupAndIndustry(incomeGroup,industry);
	}
}