package edu.utexas.wrap.assignment.sensitivity;

import java.util.Map;

import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.Node;
import edu.utexas.wrap.net.TolledBPRLink;

class CapacityDerivativeLink extends DerivativeLink {
	
	public CapacityDerivativeLink(Node tail, Node head, Float capacity, Float length, Float fftime, Link oldLink,
			Map<Link, Double> derivs) {
		super(tail, head, capacity, length, fftime, oldLink, derivs);
	}

	@Override
	public double getTravelTime() {
		Double dtdc = null;
		if (parent instanceof TolledBPRLink) {
			TolledBPRLink ll = (TolledBPRLink) parent;
			dtdc = -ll.getPower()*ll.getBValue()*ll.freeFlowTime()*Math.pow(ll.getFlow()/ll.getCapacity(), ll.getPower())/ll.getCapacity();
		}
		return deriv*flo + dtdc;
	}

}