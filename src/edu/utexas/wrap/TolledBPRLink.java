package edu.utexas.wrap;

public class TolledBPRLink extends TolledLink {
	
	private final Float b;
	private final Float power;
	protected Float toll;

	
	public TolledBPRLink(Node tail, Node head, Float capacity, Float length, Float fftime, Float b, Float power, Float toll) {
		super(tail,head,capacity,length,fftime);
		
		this.b = b;
		this.power = power;
		this.toll = toll;
	}

	public Boolean allowsClass(VehicleClass c) {
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
	public Double getPrice(Float vot, VehicleClass c) {
//		if (cachedPrice != null) return cachedPrice; // Causes a convergence failure for some reason

		cachedPrice = getTravelTime() * vot + getToll(null);
		return cachedPrice;
		
	}
	
	public Float getToll(VehicleClass c) {
		if (!allowsClass(c)) return Float.MAX_VALUE;
		return toll;
	}
	
	/**BPR Function
	 * A link performance function using empirical constants (b and power) and 
	 * link characteristics (current flow, capacity, & free flow travel time)
	 * @return travel time for the link at current flow
	 */
	public Double getTravelTime() {
		if (cachedTT == null) cachedTT = freeFlowTime()*(1+
				getBValue()*Math.pow(getFlow().doubleValue()/getCapacity(), getPower()));
		return cachedTT;
	}
	
	public Double pricePrime(Float vot) {
		return vot*tPrime() + tollPrime();
	}
	
	public Double tIntegral() {
		Float a = getPower();
		Double b = (double) getBValue();
		Double t = (double) freeFlowTime();
		Double v = getFlow();
		Float c = getCapacity();
		
		return t*v + t*b*(Math.pow(v,a+1))/((a+1)*(Math.pow(c, a)));
	}
	
	public Double tollPrime() {
		return 0.0;
	}

	/**Derivative of {@link getTravelTime} formula
	 * Calculate the derivative of the BPR function with respect to the flow
	 * @return t': the derivative of the BPR function
	 */
	public Double tPrime()  {
		// Return (a*b*t*(v/c)^a)/v
		//TODO: cache this
		Float a = getPower();
		Double b = (double) getBValue();
		Double t = (double) freeFlowTime();
		Double v = getFlow();
		Float c = getCapacity();
		Double va = Math.pow(v, a-1);
		Double ca = Math.pow(c, -a);
		return a*va*t*b*ca;
	}
}
