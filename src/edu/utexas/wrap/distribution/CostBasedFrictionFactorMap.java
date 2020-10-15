package edu.utexas.wrap.distribution;

import java.util.Map.Entry;
import java.util.NavigableMap;

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
	
	public Float get(float skimCost) {
//		Float cost = travelCosts.getCost(producer, attractor);
		if (skimCost < 0) throw new RuntimeException("Negative travel cost");
		
		//Get the nearest costFactors to this cost
		int floor = (int) Math.floor(skimCost);
		NavigableMap<Integer, Float> submap = costFactors.subMap(floor, true, floor+1,true);
		Entry<Integer, Float> lowerBd = submap.firstEntry();

		//If we landed on an exact cost for which a mapping exists, use it
		if (lowerBd != null && lowerBd.getKey() == skimCost) return lowerBd.getValue();
		
		
		//Otherwise, get the next value in the map
		Entry<Integer, Float> upperBd = submap.lastEntry();
		
		//Handle boundary cases
		if (lowerBd == null && upperBd != null) return upperBd.getValue();
		else if (lowerBd != null && upperBd == null) return lowerBd.getValue();
		else if (lowerBd == null && upperBd == null) throw new RuntimeException("No mappings in cost factor tree");

		
		//Otherwise, linearly interpolate between the two
		Float pct = (skimCost - lowerBd.getKey())/(upperBd.getKey() - lowerBd.getKey());
		
		return pct*upperBd.getValue() + (1-pct)*lowerBd.getValue();
	}

}
