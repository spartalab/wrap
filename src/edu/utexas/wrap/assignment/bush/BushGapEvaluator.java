package edu.utexas.wrap.assignment.bush;


public class BushGapEvaluator implements BushEvaluator {
	

	@Override
	public double getValue(Bush bush) {
		return bush.incurredCost()/bush.shortPathCost();
	}

}
