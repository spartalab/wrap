package edu.utexas.wrap.net;

import edu.utexas.wrap.modechoice.Mode;

public class CentroidConnector extends TolledLink {
	private float toll;

	public CentroidConnector(Node tail, Node head, Float capacity, Float length, Float fftime, Float toll) {
		super(tail, head, capacity, length, fftime);
		this.toll = toll;
	}

	@Override
	public Boolean allowsClass(Mode c) {
		return true;
	}

	@Override
	public Double getPrice(Float vot, Mode c) {
		return (double) (vot*freeFlowTime() + toll);
	}

	@Override
	public Float getToll(Mode c) {
		if (!allowsClass(c)) return Float.MAX_VALUE;
		return toll;
	}

	@Override
	public Double getTravelTime() {
		return (double) freeFlowTime();
	}

	@Override
	public Double pricePrime(Float vot) {
		return 0.0;
	}

	@Override
	public Double tIntegral() {
		return freeFlowTime()*getFlow();
	}

	@Override
	public Double tollPrime() {
		return 0.0;
	}

	@Override
	public Double tPrime() {
		return 0.0;
	}

}
