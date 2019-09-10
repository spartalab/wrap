package edu.utexas.wrap.marketsegmentation;

import java.util.function.ToDoubleFunction;

import edu.utexas.wrap.net.TravelSurveyZone;

public class ChildSegment implements ChildSegmenter {
	int numChildren;

	public ChildSegment(int children) {
		numChildren = children;
	}

	@Override
	public ToDoubleFunction<TravelSurveyZone> attributeDataGetter() {
		return tsz -> tsz.getHouseholdsByChildren(numChildren);
	}

	@Override
	public int numberOfChildren() {
		return numChildren;
	}

	public String toString() {
		return "MS: Households with "+numChildren+" children";
	}
}
