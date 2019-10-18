package edu.utexas.wrap.marketsegmentation;

import java.util.function.ToDoubleFunction;

import edu.utexas.wrap.net.TravelSurveyZone;

public class IndustrySegment implements IndustrySegmenter {

	private IndustryClass industry;
	
	public IndustrySegment(IndustryClass industry) {
		this.industry = industry;
	}
	
	@Override
	public ToDoubleFunction<TravelSurveyZone> attributeDataGetter() {
		return tsz -> tsz.getEmploymentByIndustry(industry);
	}

	@Override
	public IndustryClass getIndustryClass() {
		return industry;
	}

}
