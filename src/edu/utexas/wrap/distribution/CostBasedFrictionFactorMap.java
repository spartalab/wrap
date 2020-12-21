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


	/**
	 * @param factors a set of bins which will be used to calculate the impedance
	 * through interpolation of costs between the provided values, or the values
	 * themselves if the cost is an integer for which a value is defined
	 */
	public CostBasedFrictionFactorMap(NavigableMap<Integer, Float> factors) {
		costFactors = factors;
	}
	
	/**Get the impedance value associated with the given cost as follows:
	 * get the integer floor value of the cost; if this is equal to the cost
	 * and the impedance for it is defined, return that impedance; otherwise,
	 * get the ceiling value and interpolate between the two values. If the cost
	 * is outside the range of the bins provided at initialization, the closest
	 * value is used.
	 *
	 */
	public Float get(float skimCost) {
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
