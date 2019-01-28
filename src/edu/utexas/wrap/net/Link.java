package edu.utexas.wrap.net;

import java.util.Map;

import edu.utexas.wrap.VehicleClass;
import edu.utexas.wrap.assignment.AssignmentContainer;
import edu.utexas.wrap.assignment.bush.Bush;
import edu.utexas.wrap.util.NegativeFlowException;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2DoubleOpenHashMap;

import java.util.HashMap;

/**
 * @author rahulpatel
 *
 */
public abstract class Link implements Priced {

	private final Float capacity, length, fftime;
	private final Node head;
	private final Node tail;

	private Map<AssignmentContainer,Double> flow;

	private Double cachedFlow = null;
	protected Double cachedTT = null;
	protected Double cachedPrice = null;

	public Link(Node tail, Node head, Float capacity, Float length, Float fftime) {
		this.tail = tail;
		this.head = head;
		this.capacity = capacity;
		this.length = length;
		this.fftime = fftime;
		
		flow = new Reference2DoubleOpenHashMap<AssignmentContainer>();
	}
 

	public abstract Boolean allowsClass(VehicleClass c);

	/** Modifies the flow on a link which comes from a specified bush. 
	 * <b> THIS METHOD SHOULD ONLY BE CALLED BY THE {@link edu.utexas.wrap.assignment.bush.Bush}'s {@link edu.utexas.wrap.assignment.bush.Bush.changeFlow} METHOD </b>
	 * @param delta amount by how much the flow should be altered
	 * @param bush the origin Bush of this flow
	 * @return whether the flow from this bush on the link is non-zero
	 */
	public synchronized Boolean alterBushFlow(Double delta, Bush bush) {
		if (delta != 0) {
			cachedTT = null;
			cachedPrice = null;
			cachedFlow = null;
		}
		Double newFlow = flow.getOrDefault(bush,0.0)+ delta.doubleValue();
		if (newFlow < 0) throw new NegativeFlowException("invalid alter request");
		else if (newFlow > 0) flow.put(bush, newFlow);
		else {
			flow.remove(bush);
			return false;
		}
		return true;

	}

	public Float freeFlowTime() {
		return fftime;
	}

	public Double getFlow(AssignmentContainer source) {
		return flow.getOrDefault(source, 0.0);
	}

	public Float getCapacity() {
		return capacity;
	}

	public Double getFlow() {
		if (cachedFlow != null) return cachedFlow;
		Double f = flow.values().stream().mapToDouble(Double::doubleValue).sum();
		if (f < 0) throw new NegativeFlowException("Negative link flow");
		cachedFlow = f;
		return f;
	}

	public Node getHead() {
		return head;
	}

	public Float getLength() {
		return length;
	}
	
	public abstract Double getPrice(Float vot, VehicleClass c);

	public Node getTail() {
		return tail;
	}

	public abstract Double getTravelTime();

	public int hashCode() {
		int c = 76537;	//	UT 76-5-37 TAMC |m|
		int b = 1831;	//	Founding of Univ. of Alabama
		int a = 2017;	//	Year of inception for this project
		return (((head.getID()*a + tail.getID())*b + capacity.intValue())*c + fftime.intValue());
	}
	
	public Boolean hasFlow(Bush bush) {
		return flow.containsKey(bush);
	}

	public abstract Double pricePrime(Float float1);

	public abstract Double tIntegral();

	public String toString() {
		return tail.toString() + "\t" + head.toString();
	}
	
	public abstract Double tPrime();
}