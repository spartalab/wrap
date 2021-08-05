/*
    wrap - free and open-source urban transportation modeling software
    Copyright (C) 2017 the wrap project, The University of Texas at Austin

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
	private String name;


	/**
	 * @param factors a set of bins which will be used to calculate the impedance
	 * through interpolation of costs between the provided values, or the values
	 * themselves if the cost is an integer for which a value is defined
	 */
	public CostBasedFrictionFactorMap(String name, NavigableMap<Integer, Float> factors) {
		this.name = name;
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
	
	@Override
	public String toString() {
		return name;
	}

}
