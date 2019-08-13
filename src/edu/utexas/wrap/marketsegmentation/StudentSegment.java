package edu.utexas.wrap.marketsegmentation;

import java.util.function.ToDoubleFunction;

import edu.utexas.wrap.generation.EducationClass;
import edu.utexas.wrap.net.TravelSurveyZone;

public class StudentSegment extends MarketSegment {
	EducationClass schoolType;
	
	public StudentSegment(EducationClass schoolType, Double rate) {
		super(rate);
		this.schoolType = schoolType;
	}
	
	@Override
	public ToDoubleFunction<TravelSurveyZone> getAttributeData(){
		return tsz -> tsz.getStudentsByEducationLevel(schoolType);
	}
}