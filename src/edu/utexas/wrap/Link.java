package edu.utexas.wrap;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * @author rahulpatel
 *
 */
public abstract class Link implements Priced {

	private final Float capacity;
	private final Node head;
	private final Node tail;
	private final Float length;
	private final Float fftime;

	private Map<AssignmentContainer,BigDecimal> flow;

	private BigDecimal cachedFlow = null;
	protected BigDecimal cachedTT = null;
	protected BigDecimal cachedPrice = null;

	public Link(Node tail, Node head, Float capacity, Float length, Float fftime) {
		this.tail = tail;
		this.head = head;
		this.capacity = capacity;
		this.length = length;
		this.fftime = fftime;

		this.flow = new HashMap<AssignmentContainer,BigDecimal>();
	}


	public Float getCapacity() {
		return capacity;
	}

	public Float freeFlowTime() {
		return fftime;
	}

	public Node getHead() {
		return head;
	}

	public Node getTail() {
		return tail;
	}

	public Float getLength() {
		return length;
	}

	public BigDecimal getFlow() {
		if (cachedFlow != null) return cachedFlow;
		BigDecimal f = BigDecimal.ZERO;
		for (AssignmentContainer b : flow.keySet()) f = f.add(flow.get(b));
		if (f.compareTo(BigDecimal.ZERO) < 0) throw new NegativeFlowException("Negative link flow");
		cachedFlow = f;
		return f;
	}

	public String toString() {
		return tail.toString() + "\t" + head.toString();
	}

	public abstract BigDecimal getTravelTime();
	
	public abstract BigDecimal tPrime();

	public abstract BigDecimal tIntegral();

	public abstract BigDecimal getPrice(Float vot, VehicleClass c);

	public abstract BigDecimal pricePrime(Float float1);

	/** Modifies the flow on a link which comes from a specified bush. 
	 * <b> THIS METHOD SHOULD ONLY BE CALLED BY THE {@link edu.utexas.wrap.Bush}'s {@link edu.utexas.wrap.Bush.changeFlow} METHOD </b>
	 * @param delta amount by how much the flow should be altered
	 * @param bush the origin Bush of this flow
	 * @return whether the flow from this bush on the link is non-zero
	 */
	public synchronized Boolean alterBushFlow(BigDecimal delta, Bush bush) {
		if (delta.compareTo(BigDecimal.ZERO) != 0) {
			cachedTT = null;
			cachedPrice = null;
			cachedFlow = null;
		}
		BigDecimal newFlow = flow.getOrDefault(bush,BigDecimal.ZERO).add(delta).setScale(Optimizer.decimalPlaces, RoundingMode.HALF_EVEN);
		if (newFlow.compareTo(BigDecimal.ZERO) < 0) throw new NegativeFlowException("invalid alter request");
		else if (newFlow.compareTo(BigDecimal.ZERO) > 0) flow.put(bush, newFlow);
		else {
			flow.remove(bush);
			return false;
		}
		return true;

	}

	public BigDecimal getBushFlow(Bush bush) {
		return flow.getOrDefault(bush, BigDecimal.ZERO);
	}

	public Boolean hasFlow(Bush bush) {
		return flow.get(bush) != null;
	}
	
	public abstract Boolean allowsClass(VehicleClass c);
}