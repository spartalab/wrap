package edu.utexas.wrap.marketsegmentation;

import java.util.function.ToDoubleFunction;

import edu.utexas.wrap.net.TravelSurveyZone;

public class IncomeGroupIndustrySegment implements IncomeGroupSegmenter, IndustrySegmenter {

	private int incomeGroup;
	private IndustryClass industry;
	
	public IncomeGroupIndustrySegment(Integer incomeGrp, IndustryClass industry) {
		incomeGroup = incomeGrp;
		this.industry = industry;
	}
	
	@Override
	public ToDoubleFunction<TravelSurveyZone> attributeDataGetter() {
		return tsz -> tsz.getEmploymentByIncomeGroupAndIndustry(incomeGroup,industry);
	}

	@Override
	public IndustryClass getIndustryClass() {
		return industry;
	}

	@Override
	public int getIncomeGroup() {
		return incomeGroup;
	}
	
	public String toString() {
		return "MS:"+industry.name()+" employment in income groupd "+incomeGroup;
	}
}