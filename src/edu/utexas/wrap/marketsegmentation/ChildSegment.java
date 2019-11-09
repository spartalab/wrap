package edu.utexas.wrap.marketsegmentation;

import java.util.function.ToDoubleFunction;

import edu.utexas.wrap.net.TravelSurveyZone;

public class ChildSegment implements ChildSegmenter {
	private int numChildren;
	private int hash;

	public ChildSegment(int children) {
		numChildren = children;
	}

	public ChildSegment(String arguments) {
		String[] args = arguments.split(",");
		if(args.length != 1) {
			throw new IllegalArgumentException("Mismatch number of arguments expected 1 got " + args.length);
		}
		this.numChildren = Integer.parseInt(args[0]);
	}

	@Override
	public ToDoubleFunction<TravelSurveyZone> attributeDataGetter() {
		return tsz -> tsz.getHouseholdsByChildren(numChildren);
	}

	@Override
	public int numberOfChildren() {
		return numChildren;
	}

	public String toString() { return "MS: Households with " + numChildren + " children"; }

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
			ChildSegment other = (ChildSegment) obj;
			return other.numChildren == numChildren;
		} catch (ClassCastException e) {
			return false;
		}
	}
}
