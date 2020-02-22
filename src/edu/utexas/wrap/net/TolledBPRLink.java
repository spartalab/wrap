package edu.utexas.wrap.net;

import edu.utexas.wrap.assignment.AssignmentContainer;
import edu.utexas.wrap.modechoice.Mode;

public class TolledBPRLink extends TolledLink {
	
	private final float b, power, toll;
	private final Double tp;

	
	public TolledBPRLink(Node tail, Node head, Float capacity, Float length, Float fftime, Float b, Float power, Float toll) {
		super(tail,head,capacity,length,fftime);
		
		this.b = b;
		this.power = power;
		this.toll = toll;
		
		Double ca = Math.pow(capacity, -power);
		tp = power*fftime*b*ca;
	}

	public Boolean allowsClass(Mode c) {
		return true;
	}

	//B and power are empirical constants in the BPR function
	public Float getBValue() {
		return this.b;
	}
	
	public Float getPower() {
		return power;
	}
	
	@Override
	public double getPrice(Float vot, Mode c) {
//		if (cachedPrice != null) return cachedPrice; // Causes a convergence failure for some reason

		return getTravelTime() * vot + getToll(null);
		
	}
	
	public Float getToll(Mode c) {
		if (!allowsClass(c)) return Float.MAX_VALUE;
		return toll;
	}
	
	/**BPR Function
	 * A link performance function using empirical constants (b and power) and 
	 * link characteristics (current flow, capacity, & free flow travel time)
	 * @return travel time for the link at current flow
	 */
	public double getTravelTime() {
		if (cachedTT == null) cachedTT = freeFlowTime()*(1+
				b*Math.pow(getFlow()/getCapacity(), power));
		return cachedTT;
	}
	
	public double pricePrime(Float vot) {
		Double r = vot*tPrime() + tollPrime();
		if (r.isNaN()) {
			throw new RuntimeException();
		}
		return r;
	}
	
	public double tIntegral() {
		Float a = getPower();
		Double b = (double) getBValue();
		Double t = (double) freeFlowTime();
		Double v = getFlow();
		Float c = getCapacity();
		
		return t*v + t*b*(Math.pow(v,a+1))/((a+1)*(Math.pow(c, a)));
	}
	
	public double tollPrime() {
		return 0.0;
	}

	/**Derivative of {@link getTravelTime} formula
	 * Calculate the derivative of the BPR function with respect to the flow
	 * @return t': the derivative of the BPR function
	 */
	public double tPrime()  {
		// Return (a*b*t*(v/c)^a)/v
		//TODO: cache this		
		if (cachedTP != null) return cachedTP;
		Double va = Math.pow(getFlow(), power-1);

		Double r = va*tp;
		if (r.isNaN()) {
			throw new RuntimeException("Invalid BPR parameters");
		}
		cachedTP = r;
		return r;
	}

	@Override
	public double getPrice(AssignmentContainer container) {
		// TODO Auto-generated method stub
		return getPrice(container.valueOfTime(),container.vehicleClass());
	}
}
