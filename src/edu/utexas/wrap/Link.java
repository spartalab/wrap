package edu.utexas.wrap;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * @author rahulpatel
 *
 */
public abstract class Link implements Priced {

	private final Double capacity;
	private final Node head;
	private final Node tail;
	private final Double length;
	private final Double fftime;

	public final Semaphore lock;
	private Map<Bush,BigDecimal> flow;

	private BigDecimal cachedFlow = null;
	protected BigDecimal cachedTT = null;
	protected BigDecimal cachedPrice = null;

	public Link(Node tail, Node head, Double capacity, Double length, Double fftime) {
		this.tail = tail;
		this.head = head;
		this.capacity = capacity;
		this.length = length;
		this.fftime = fftime;

		this.flow = new HashMap<Bush,BigDecimal>();
		lock = new Semaphore(1);
	}


	public Double getCapacity() {
		return capacity;
	}

	public Double freeFlowTime() {
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

	public String toString() {
		return tail.toString() + "\t" + head.toString();
	}

	public abstract BigDecimal getTravelTime();
	
	public abstract BigDecimal tPrime();

	public abstract BigDecimal tIntegral();

	public abstract BigDecimal getPrice(Double vot, VehicleClass c);

	public abstract BigDecimal pricePrime(Double vot);

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

enum VehicleClass {
	MED_TRUCK, HVY_TRUCK, SINGLE_OCC, HIGH_OCC
}