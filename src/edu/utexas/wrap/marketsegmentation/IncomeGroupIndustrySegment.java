package edu.utexas.wrap.marketsegmentation;

import java.util.function.ToDoubleFunction;

import edu.utexas.wrap.net.TravelSurveyZone;

public class IncomeGroupIndustrySegment implements IncomeGroupSegmenter, IndustrySegmenter {

	private int incomeGroup;
	private IndustryClass industry;
	private int hash;
	
	public IncomeGroupIndustrySegment(Integer incomeGrp, IndustryClass industry) {
		incomeGroup = incomeGrp;
		this.industry = industry;
	}

	public IncomeGroupIndustrySegment(String arguments) {
		String[] args = arguments.split(",");
		if(args.length != 2) {
			throw new IllegalArgumentException("Mismatch number of arguments expected 2 got " + args.length);
		}
		this.incomeGroup = Integer.parseInt(args[0]);
		if (args[1].equalsIgnoreCase("b")) {
			this.industry = IndustryClass.BASIC;
		}
		else if (args[1].equalsIgnoreCase("r")) {
			this.industry = IndustryClass.RETAIL;
		}
		else if (args[1].equalsIgnoreCase("s")) {
			this.industry = IndustryClass.SERVICE;
		}
		else this.industry = IndustryClass.valueOf(args[1]);
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
		return "MS:"+industry.name()+" employment in income group "+incomeGroup;
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
			IncomeGroupIndustrySegment other = (IncomeGroupIndustrySegment) obj;
			return other.incomeGroup == incomeGroup && other.industry.equals(industry);
		} catch (ClassCastException e) {
			return false;
		}
	}
}