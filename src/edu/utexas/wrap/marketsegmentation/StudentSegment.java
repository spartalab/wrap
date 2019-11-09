package edu.utexas.wrap.marketsegmentation;

import java.util.function.ToDoubleFunction;

import edu.utexas.wrap.net.TravelSurveyZone;

public class StudentSegment implements StudentSegmenter {
	private EducationClass schoolType;
	private int hash;
	
	public StudentSegment(EducationClass schoolType) {
		this.schoolType = schoolType;
	}

	public StudentSegment(String arguments) {
		String[] args = arguments.split(",");
		if(args.length != 1) {
			throw new IllegalArgumentException("Mismatch number of arguments expected 1 got " + args.length);
		}
		this.schoolType = EducationClass.valueOf(args[0]);
	}

	@Override
	public ToDoubleFunction<TravelSurveyZone> attributeDataGetter(){
		return tsz -> tsz.getStudentsByEducationLevel(schoolType);
	}

	@Override
	public EducationClass getEducationClass() {
		return schoolType;
	}
	
	public String toString() {
		return "Students in education level "+schoolType.name();
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
			StudentSegment other = (StudentSegment) obj;
			return other.schoolType.equals(schoolType);
		} catch (ClassCastException e) {
			return false;
		}
	}
}