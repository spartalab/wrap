package edu.utexas.wrap;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
	private Map<Bush,BigDecimal> flow;
	private Double toll;
	private BigDecimal cachedFlow = null;
	private BigDecimal cachedTT = null;
	private BigDecimal cachedPrice = null;

	public Link(Node tail, Node head, Double capacity, Double length, Double fftime, Double b, Double power, Double toll) {
		this.tail = tail;
		this.head = head;
		this.capacity = capacity;
		this.length = length;
		this.fftime = fftime;
		this.b = b;
		this.power = power;
		this.flow = new HashMap<Bush,BigDecimal>();
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

	public BigDecimal getFlow() {
		if (cachedFlow != null) return cachedFlow;
		BigDecimal f = BigDecimal.ZERO;
		for (Bush b : flow.keySet()) f = f.add(flow.get(b));
		if (f.compareTo(BigDecimal.ZERO) < 0) throw new NegativeFlowException("Negative link flow");
		cachedFlow = f;
		return f;
	}

	/**BPR Function
	 * A link performance function using empirical constants (b and power) and 
	 * link characteristics (current flow, capacity, & free flow travel time)
	 * @return travel time for the link at current flow
	 */
	public BigDecimal getTravelTime() {
		if (cachedTT != null) return cachedTT;
		cachedTT = BigDecimal.valueOf(getFfTime()).multiply(BigDecimal.ONE.add(
				BigDecimal.valueOf(getBValue()*Math.pow(getFlow().doubleValue()/getCapacity(), getPower()))
				));
		return cachedTT;
	}
	
	public String toString() {
		return tail.toString() + "\t" + head.toString();
	}

	/**Derivative of {@link getTravelTime} formula
	 * Calculate the derivative of the BPR function with respect to the flow
	 * @return t': the derivative of the BPR function
	 */
	public BigDecimal tPrime()  {
		// Return (a*b*t*(v/c)^a)/v
		//TODO: cache this
		Double a = getPower();
		BigDecimal b = BigDecimal.valueOf(getBValue());
		BigDecimal t = BigDecimal.valueOf(getFfTime());
		Double v = getFlow().doubleValue();
		Double c = getCapacity();
		BigDecimal va = BigDecimal.valueOf(Math.pow(v, a-1.0));
		BigDecimal ca = BigDecimal.valueOf(Math.pow(c, -a));
		return BigDecimal.valueOf(a).multiply(va).multiply(t).multiply(b).multiply(ca);
	}
	
	public BigDecimal tIntegral() {
		Double a = getPower();
		BigDecimal b = BigDecimal.valueOf(getBValue());
		BigDecimal t = BigDecimal.valueOf(getFfTime());
		Double v = getFlow().doubleValue();
		Double c = getCapacity();
		
		//return t*v + t*b*(Math.pow(v,a+1))/((a+1)*(Math.pow(c, a)));
		return t.multiply(getFlow()).add(	// t*v + (
				t.multiply(b).multiply(BigDecimal.valueOf(Math.pow(v, a+1))).divide(	// t*b*(v^a+1)/(
						BigDecimal.valueOf(a).add(BigDecimal.ONE).multiply(BigDecimal.valueOf(Math.pow(c, a))),	// a+1*(c^a)
						RoundingMode.HALF_EVEN)											// )
				);							// )
	}
	
	public Double getToll() {
		return toll;
	}
	
	public BigDecimal tollPrime() {
		//TODO: Modify this if tolls change in response to flow
		return BigDecimal.ZERO;
	}

	@Override
	public BigDecimal getPrice(Double vot) {
		if (cachedPrice != null) return cachedPrice;
		try {
			return getTravelTime().multiply(BigDecimal.valueOf(vot)).add(BigDecimal.valueOf(getToll()));
		}
		catch (Exception e) {
			e.printStackTrace();
			return BigDecimal.ZERO;
		}
	}
	
	public BigDecimal pricePrime(Double vot) {
			return BigDecimal.valueOf(vot).multiply(tPrime()).add(tollPrime());
	}

	public synchronized void alterBushFlow(BigDecimal delta, Bush bush) {
		BigDecimal newFlow = flow.getOrDefault(bush,BigDecimal.ZERO).add(delta).setScale(Optimizer.decimalPlaces, RoundingMode.HALF_EVEN);
		if (newFlow.compareTo(BigDecimal.ZERO) < 0) throw new NegativeFlowException("invalid alter request");
		else if (newFlow.compareTo(BigDecimal.ZERO) > 0) flow.put(bush, newFlow);
		else flow.remove(bush);
		if (delta.compareTo(BigDecimal.ZERO) != 0) {
			cachedTT = null;
			cachedPrice = null;
			cachedFlow = null;
		}
	}

	public BigDecimal getBushFlow(Bush bush) {
		return flow.getOrDefault(bush, BigDecimal.ZERO);
	}
}
