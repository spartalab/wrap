package edu.utexas.wrap.distribution;

import java.util.Map;
import java.util.TreeMap;

import edu.utexas.wrap.net.Node;
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

	private float[][] travelCosts;
	private TreeMap<Integer, Float> costFactors;


	public CostBasedFrictionFactorMap(float[][] costSkim, TreeMap<Integer, Float> factors) {
		travelCosts = costSkim;
		costFactors = factors;
	}
	
	@Override
	public Float get(TravelSurveyZone producer, TravelSurveyZone attractor) {
		Float cost = travelCosts[producer.getOrder()][attractor.getOrder()];
		if (cost < 0) throw new RuntimeException("Negative travel cost");
		
		//Get the nearest costFactors to this cost
		Integer lowerBd = costFactors.floorKey((int) Math.floor(cost));
		Integer upperBd = costFactors.ceilingKey((int) Math.ceil(cost));
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
		Float pct = (cost - lowerBd)/(upperBd - lowerBd);
		
		 c = pct*costFactors.get(upperBd) + (1-pct)*costFactors.get(lowerBd);
		if (c.isNaN()) throw new RuntimeException();
		return c;
	}

}
