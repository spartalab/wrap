package edu.utexas.wrap.net;

import edu.utexas.wrap.assignment.AssignmentContainer;
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
	public double getPrice(Float vot, Mode c) {
		return (double) (vot*freeFlowTime() + toll);
	}

	@Override
	public Float getToll(Mode c) {
		if (!allowsClass(c)) return Float.MAX_VALUE;
		return toll;
	}

	@Override
	public double getTravelTime() {
		return (double) freeFlowTime();
	}

	@Override
	public double pricePrime(Float vot) {
		return 0.0;
	}

	@Override
	public double tIntegral() {
		return freeFlowTime()*getFlow();
	}

	@Override
	public double tollPrime() {
		return 0.0;
	}

	@Override
	public double tPrime() {
		return 0.0;
	}

	@Override
	public double getPrice(AssignmentContainer container) {
		return getPrice(container.valueOfTime(),container.vehicleClass());
	}

}
