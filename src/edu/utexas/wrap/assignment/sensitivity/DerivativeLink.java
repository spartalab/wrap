package edu.utexas.wrap.assignment.sensitivity;

import java.util.Map;

import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.Node;

public class DerivativeLink extends Link {
	protected final Link parent;
	Double deriv;

	public DerivativeLink(Node tail, Node head, Float capacity, Float length, Float fftime, Link oldLink,
			Map<Link, Double> derivs) {
		super(tail, head, capacity, length, fftime);
		parent = oldLink;
		deriv = derivs.getOrDefault(oldLink, 0.0);
	}

	@Override
	public Boolean allowsClass(Mode c) {
		return parent.allowsClass(c);
	}

	@Override
	public double getPrice(Float vot, Mode c) {
		return getTravelTime();
	}

	@Override
	public double getTravelTime() {
		return deriv*flo;
	}

	@Override
	public double pricePrime(Float vot) {
		return tPrime();
	}

	@Override
	public double tIntegral() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double tPrime() {
		return deriv;
	}

	@Override
	public Boolean changeFlow(Double delta) {
		flo += delta;
		if (flo.isNaN()) {
			throw new RuntimeException();
		}
		if (delta != 0.0) {
			cachedTT = null;
			cachedTP = null;
		}
		return flo > 0.0;
	}
	
	@Override
	public Double getFlow() {
		return flo;
	}
}