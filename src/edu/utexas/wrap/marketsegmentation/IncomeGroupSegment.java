package edu.utexas.wrap.marketsegmentation;

import java.util.function.ToDoubleFunction;

import edu.utexas.wrap.net.TravelSurveyZone;

public class IncomeGroupSegment implements IncomeGroupSegmenter {
	private int incomeGroup;
	private int hash;
	
	public IncomeGroupSegment(Integer incomeGrp) {
		incomeGroup = incomeGrp;
	}

	public IncomeGroupSegment(String arguments) {
		String[] args = arguments.split(",");
		if(args.length != 1) {
			throw new IllegalArgumentException("Mismatch number of arguments expected 1 got " + args.length);
		}
		this.incomeGroup = Integer.parseInt(args[0]);
	}
	
	@Override
	public ToDoubleFunction<TravelSurveyZone> attributeDataGetter() {
		return tsz -> tsz.getHouseholdsByIncomeGroup(incomeGroup);
	}

	@Override
	public int getIncomeGroup() {
		return incomeGroup;
	}
	
	public String toString() {
		return "MS: Households in income group "+incomeGroup;
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
			IncomeGroupSegmenter other = (IncomeGroupSegmenter) obj;
			return other.getIncomeGroup() == incomeGroup;
		} catch (ClassCastException e) {
			return false;
		}
	}
}