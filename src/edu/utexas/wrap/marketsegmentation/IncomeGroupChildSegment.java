package edu.utexas.wrap.marketsegmentation;

import java.util.function.ToDoubleFunction;

import edu.utexas.wrap.net.TravelSurveyZone;

public class IncomeGroupChildSegment implements ChildSegmenter, IncomeGroupSegmenter {
	private int incomeGroup, numChildren, hash;
	
	public IncomeGroupChildSegment(int incomeGrp, int children) {
		incomeGroup = incomeGrp;
		numChildren = children;
	}
	
	public IncomeGroupChildSegment(String arguments) {
		String[] args = arguments.split(",");
		if(args.length != 2) {
			throw new IllegalArgumentException("Mismatch number of arguments expected 2 got " + args.length);
		}
		this.incomeGroup = Integer.parseInt(args[0]);
		this.numChildren = Integer.parseInt(args[1]);
	}

	@Override
	public ToDoubleFunction<TravelSurveyZone> attributeDataGetter() {
		// TODO Auto-generated method stub
		return tsz -> tsz.getHouseholdsByIncomeGroupThenChildren(incomeGroup,numChildren);
	}

	@Override
	public int getIncomeGroup() {
		// TODO Auto-generated method stub
		return incomeGroup;
	}

	@Override
	public int numberOfChildren() {
		// TODO Auto-generated method stub
		return numChildren;
	}
	
	public String toString() {
		return "MS: Households in income group "+incomeGroup+" with "+numChildren+" children";
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
			IncomeGroupChildSegment other = (IncomeGroupChildSegment) obj;
			return other.incomeGroup == incomeGroup && other.numChildren == numChildren ;
		} catch (ClassCastException e) {
			return false;
		}
	}

}
