package edu.utexas.wrap.distribution;

import java.util.Map;
import java.util.TreeMap;

import edu.utexas.wrap.net.Node;

public class CostBasedFrictionFactorMap implements FrictionFactorMap {

	private Map<Node, Map<Node, Float>> travelCosts;
	private TreeMap<Integer, Float> costFactors;


	public CostBasedFrictionFactorMap(Map<Node,Map<Node,Float>> costSkim, TreeMap<Integer, Float> factors) {
		travelCosts = costSkim;
		costFactors = factors;
	}
	
	@Override
	public Float get(Node producer, Node attractor) {
		Float cost = travelCosts.get(producer).get(attractor);
		if (cost < 0) throw new RuntimeException("Negative travel cost");
		
		Integer lowerBd = costFactors.floorKey((int) Math.floor(cost));
		Integer upperBd = costFactors.ceilingKey((int) Math.ceil(cost));
		Float c;
		if (lowerBd == null && upperBd != null) lowerBd = upperBd;
		else if (lowerBd != null && upperBd == null) upperBd = lowerBd;
		else if (lowerBd == null && upperBd == null) throw new RuntimeException("No mappings in cost factor tree");
		if (lowerBd == upperBd) {
			c = costFactors.get(lowerBd);
			if (c.isNaN()) throw new RuntimeException();
			return c;
		}

		Float pct = (cost - lowerBd)/(upperBd - lowerBd);
		
		 c = pct*costFactors.get(upperBd) + (1-pct)*costFactors.get(lowerBd);
		if (c.isNaN()) throw new RuntimeException();
		return c;
	}

}
