package edu.utexas.wrap.marketsegmentation;

import java.util.function.ToDoubleFunction;

import edu.utexas.wrap.net.TravelSurveyZone;

public class ChildSegment extends MarketSegment {
	int numChildren;

	public ChildSegment(int children, Double segmentRate) {
		// TODO Auto-generated constructor stub
		super(segmentRate);
		numChildren = children;
	}

	@Override
	public ToDoubleFunction<TravelSurveyZone> getAttributeData() {
		return tsz -> tsz.getHouseholdsByChildren(numChildren);
	}

}