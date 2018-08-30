package edu.utexas.wrap;

import java.math.BigDecimal;

import org.mapdb.DB;

public class TolledBPRLink extends TolledLink {
	
	private final Float b;
	private final Float power;
	protected Float toll;

	
	public TolledBPRLink(DB db, Node tail, Node head, Float capacity, Float length, Float fftime, Float b, Float power, Float toll) {
		super(db,tail,head,capacity,length,fftime);
		
		this.b = b;
		this.power = power;
		this.toll = toll;
	}

	//B and power are empirical constants in the BPR function
	public Float getBValue() {
		return this.b;
	}

	public Float getPower() {
		return power;
	}
	
	/**BPR Function
	 * A link performance function using empirical constants (b and power) and 
	 * link characteristics (current flow, capacity, & free flow travel time)
	 * @return travel time for the link at current flow
	 */
	public BigDecimal getTravelTime() {
		if (cachedTT != null) return cachedTT;
		cachedTT = BigDecimal.valueOf(freeFlowTime()).multiply(BigDecimal.ONE.add(
				BigDecimal.valueOf(getBValue()*Math.pow(getFlow().doubleValue()/getCapacity(), getPower()))),
				Optimizer.defMC);
		return cachedTT;
	}
	
	/**Derivative of {@link getTravelTime} formula
	 * Calculate the derivative of the BPR function with respect to the flow
	 * @return t': the derivative of the BPR function
	 */
	public BigDecimal tPrime()  {
		// Return (a*b*t*(v/c)^a)/v
		//TODO: cache this
		Float a = getPower();
		BigDecimal b = BigDecimal.valueOf(getBValue());
		BigDecimal t = BigDecimal.valueOf(freeFlowTime());
		Double v = getFlow().doubleValue();
		Float c = getCapacity();
		BigDecimal va = BigDecimal.valueOf(Math.pow(v, a-1.0));
		BigDecimal ca = BigDecimal.valueOf(Math.pow(c, -a));
		return BigDecimal.valueOf(a).multiply(va).multiply(t).multiply(b).multiply(ca, Optimizer.defMC);
	}
	
	public BigDecimal tIntegral() {
		Float a = getPower();
		BigDecimal b = BigDecimal.valueOf(getBValue());
		BigDecimal t = BigDecimal.valueOf(freeFlowTime());
		Double v = getFlow().doubleValue();
		Float c = getCapacity();
		
		//return t*v + t*b*(Math.pow(v,a+1))/((a+1)*(Math.pow(c, a)));
		return t.multiply(getFlow()).add(	// t*v + (
				t.multiply(b).multiply(BigDecimal.valueOf(Math.pow(v, a+1))).divide(	// t*b*(v^a+1)/(
						BigDecimal.valueOf(a).add(BigDecimal.ONE).multiply(BigDecimal.valueOf(Math.pow(c, a))),	// a+1*(c^a)
						Optimizer.defMC)											// )
				);							// )
	}
	
	public Float getToll(VehicleClass c) {
		return toll;
	}
	
	public BigDecimal tollPrime() {
		return BigDecimal.ZERO;
	}
	
	@Override
	public BigDecimal getPrice(Float vot, VehicleClass c) {
		if (cachedPrice != null) return cachedPrice;
		try {
			return getTravelTime().multiply(BigDecimal.valueOf(vot), Optimizer.defMC).add(BigDecimal.valueOf(getToll(null)));
		}
		catch (Exception e) {
			e.printStackTrace();
			return BigDecimal.ZERO;
		}
	}
	
	public BigDecimal pricePrime(Float vot) {
		return BigDecimal.valueOf(vot).multiply(tPrime(), Optimizer.defMC).add(tollPrime());
	}

	public Boolean allowsClass(VehicleClass c) {
		return true;
	}
}
