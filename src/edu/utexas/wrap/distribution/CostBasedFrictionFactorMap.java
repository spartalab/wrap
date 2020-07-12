package edu.utexas.wrap.distribution;

import java.util.NavigableMap;

import edu.utexas.wrap.net.TravelSurveyZone;

/**A friction factor map that depends on a cost skim and
 * an ordered set of multiplication factors. The impedance
 * between two TSZs is determined by the travel cost between
 * the two, which is then used to determine the impedance
 * from a given set of pre-determined points.
 * 
 * @author William
 *
 */
public class CostBasedFrictionFactorMap implements FrictionFactorMap {

	private NavigableMap<Integer, Float> costFactors;


	public CostBasedFrictionFactorMap(NavigableMap<Integer, Float> factors) {
		costFactors = factors;
	}
	
	@Override
	public Float get(TravelSurveyZone producer, TravelSurveyZone attractor, float skimCost) {
//		Float cost = travelCosts.getCost(producer, attractor);
		if (skimCost < 0) throw new RuntimeException("Negative travel cost");
		
		//Get the nearest costFactors to this cost
		Integer lowerBd = costFactors.floorKey((int) Math.floor(skimCost));
		Integer upperBd = costFactors.ceilingKey((int) Math.ceil(skimCost));
		Float c;
		
		//Handle boundary cases
		if (lowerBd == null && upperBd != null) lowerBd = upperBd;
		else if (lowerBd != null && upperBd == null) upperBd = lowerBd;
		else if (lowerBd == null && upperBd == null) throw new RuntimeException("No mappings in cost factor tree");

		//If we landed on an exact cost for which a mapping exists, use it
		if (lowerBd == upperBd) {
			c = costFactors.get(lowerBd);
			if (c.isNaN()) throw new RuntimeException();
			return c;
		}
		
		//Otherwise, linearly interpolate between the two
		Float pct = (skimCost - lowerBd)/(upperBd - lowerBd);
		
		 c = pct*costFactors.get(upperBd) + (1-pct)*costFactors.get(lowerBd);
		if (c.isNaN()) throw new RuntimeException();
		return c;
	}

}
