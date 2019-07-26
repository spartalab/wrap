package edu.utexas.wrap.assignment.sensitivity;

import java.util.Map;

import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.Node;
import edu.utexas.wrap.net.TolledBPRLink;

class CapacityDerivativeLink extends Link {
	private final Link parent;
	Double deriv;

	CapacityDerivativeLink(Node tail, Node head, Float capacity, Float length, Float fftime, Link l,
			Map<Link, Double> dtdX) {
		super(tail, head, capacity, length, fftime);
		parent = l;
		deriv = dtdX.getOrDefault(l,0.0);
	}

	@Override
	public Boolean allowsClass(Mode c) {
		// TODO Auto-generated method stub
		return parent.allowsClass(c);
	}

	@Override
	public double getPrice(Float vot, Mode c) {
		// TODO Auto-generated method stub
		return getTravelTime();
	}

	@Override
	public double getTravelTime() {
		// TODO Auto-generated method stub
		Double dtdc = null;
		if (parent instanceof TolledBPRLink) {
			TolledBPRLink ll = (TolledBPRLink) parent;
			dtdc = -ll.getPower()*ll.getBValue()*ll.freeFlowTime()*Math.pow(ll.getFlow()/ll.getCapacity(), ll.getPower())/ll.getCapacity();
		}
		return deriv*flo + dtdc;
	}

	@Override
	public double pricePrime(Float vot) {
		// TODO Auto-generated method stub
		return tPrime();
	}

	@Override
	public double tIntegral() {
		// TODO Auto-generated method stub
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public double tPrime() {
		// TODO Auto-generated method stub
		return deriv;
	}

	@Override
	public boolean nonnegativeFlowLink() {
		return false;
	}
}