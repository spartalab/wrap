package edu.utexas.wrap.net;

import edu.utexas.wrap.assignment.AssignmentContainer;
import edu.utexas.wrap.modechoice.Mode;

public class CentroidConnector extends TolledLink {
	private float toll;

	public CentroidConnector(Node tail, Node head, Float capacity, Float length, Float fftime, Float toll, Integer linkID) {
		super(tail, head, capacity, length, fftime, linkID);
		this.toll = toll;
	}

	public Boolean allowsClass(Mode c) {
		return true;
	}

	public double getPrice(Float vot, Mode c) {
		return (double) (vot*freeFlowTime() + toll);
	}

	public Float getToll(Mode c) {
		if (!allowsClass(c)) return Float.MAX_VALUE;
		return toll;
	}

	public double getTravelTime() {
		return (double) freeFlowTime();
	}

	public double pricePrime(Float vot) {
		return 0.0;
	}

	public double tIntegral() {
		return freeFlowTime()*getFlow();
	}

	public double tollPrime() {
		return 0.0;
	}

	public double tPrime() {
		return 0.0;
	}

	public double getPrice(AssignmentContainer container) {
		return getPrice(container.valueOfTime(),container.vehicleClass());
	}

}
