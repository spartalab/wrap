package edu.utexas.wrap.marketsegmentation;

import java.util.function.ToDoubleFunction;

import edu.utexas.wrap.net.TravelSurveyZone;

public class StudentSegment implements StudentSegmenter {
	EducationClass schoolType;
	
	public StudentSegment(EducationClass schoolType) {
		this.schoolType = schoolType;
	}
	
	@Override
	public ToDoubleFunction<TravelSurveyZone> attributeDataGetter(){
		return tsz -> tsz.getStudentsByEducationLevel(schoolType);
	}

	@Override
	public EducationClass getEducationClass() {
		return schoolType;
	}
}