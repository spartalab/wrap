package edu.utexas.wrap.marketsegmentation;

import java.util.function.ToDoubleFunction;

import edu.utexas.wrap.net.TravelSurveyZone;

public class IndustrySegment implements IndustrySegmenter {

	private IndustryClass industry;
	private int hash;
	
	public IndustrySegment(IndustryClass industry) {
		this.industry = industry;
	}

	public IndustrySegment(String arguments) {
		String[] args = arguments.split(",");
		if(args.length != 1) {
			throw new IllegalArgumentException("Mismatch number of arguments expected 1 got " + args.length);
		}
		this.industry = IndustryClass.valueOf(args[0]);
	}

	@Override
	public ToDoubleFunction<TravelSurveyZone> attributeDataGetter() {
		return tsz -> tsz.getEmploymentByIndustry(industry);
	}

	@Override
	public IndustryClass getIndustryClass() {
		return industry;
	}

	@Override
	public String toString() {
		return "MS: Industry of type " + industry ;
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
			IndustrySegment other = (IndustrySegment) obj;
			return other.industry.equals(industry);
		} catch (ClassCastException e) {
			return false;
		}
	}

}
