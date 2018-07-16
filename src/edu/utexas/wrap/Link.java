package edu.utexas.wrap;

import java.util.HashMap;
import java.util.Map;

/**
 * @author rahulpatel
 *
 */
public class Link implements Priced {

	private final Double capacity;
	private final Node head;
	private final Node tail;
	private final Double length;
	private final Double fftime;
	private final Double b;
	private final Double power;
	private Map<Bush,Double> flow;
	private Double toll;
	private Double cachedTT = null;

	public Link(Node tail, Node head, Double capacity, Double length, Double fftime, Double b, Double power, Double toll) {
		this.tail = tail;
		this.head = head;
		this.capacity = capacity;
		this.length = length;
		this.fftime = fftime;
		this.b = b;
		this.power = power;
		this.flow = new HashMap<Bush,Double>();
		this.toll = toll;
	}

	//B and power are empirical constants in the BPR function
	public Double getBValue() {
		return this.b;
	}

	public Double getPower() {
		return power;
	}

	public Double getCapacity() {
		return capacity;
	}

	public Double getFfTime() {
		return fftime;
	}
	
	public Node getHead() {
		return head;
	}

	public Node getTail() {
		return tail;
	}

	public Double getLength() {
		return length;
	}

	public Double getFlow() {
		Double f = 0.0;
		for (Bush b : flow.keySet()) f+=flow.get(b);
		if (f < 0.0) throw new NegativeFlowException("Negative link flow");
		
		return f;
	}

	/**BPR Function
	 * A link performance function using empirical constants (b and power) and 
	 * link characteristics (current flow, capacity, & free flow travel time)
	 * @return travel time for the link at current flow
	 */
	public Double getTravelTime() {
		if (cachedTT != null) return cachedTT;
		cachedTT = getFfTime()*(1.0 + getBValue()*Math.pow(getFlow()/getCapacity(), getPower()));
		return cachedTT;
	}
	
	public String toString() {
		return tail.toString() + "\t" + head.toString();
	}

	/**Derivative of {@link getTravelTime} formula
	 * Calculate the derivative of the BPR function with respect to the flow
	 * @return t': the derivative of the BPR function
	 */
	public Double tPrime()  {
		// Return (a*b*t*(v/c)^a)/v
		//TODO: cache this
		Double a = getPower();
		Double b = getBValue();
		Double t = getFfTime();
		Double v = getFlow();
		Double c = getCapacity();
		Double va = Math.pow(v, a-1.0);
		Double ca = Math.pow(c, -a);
		return a*va*t*b*ca;
	}
	
	public Double tIntegral() {
		Double a = getPower();
		Double b = getBValue();
		Double t = getFfTime();
		Double v = getFlow();
		Double c = getCapacity();
		
		return t*v + t*b*(Math.pow(v,a+1))/((a+1)*(Math.pow(c, a)));
	}
	
	public Double getToll() {
		return toll;
	}
	
	public Double tollPrime() {
		//TODO: Modify this if tolls change in response to flow
		return 0.0;
	}

	@Override
	public Double getPrice(Double vot) {
		try {
			return getTravelTime() * vot + getToll();
		}
		catch (Exception e) {
			e.printStackTrace();
			return 0.0;
		}
	}
	
	public Double pricePrime(Double vot) {
			return vot * tPrime() + tollPrime();
	}

	public synchronized void alterBushFlow(Double delta, Bush bush) {
		Double newFlow = flow.getOrDefault(bush,0.0) + delta;
		if (newFlow < 0.0) throw new NegativeFlowException("invalid alter request");
		else if (newFlow > 0.0) flow.put(bush, newFlow);
		else flow.remove(bush);
		if (delta != 0.0) cachedTT = null;
	}

	public Double getBushFlow(Bush bush) {
		return flow.getOrDefault(bush, 0.0);
	}
}
