/*
    wrap - free and open-source urban transportation modeling software
    Copyright (C) 2017 the wrap project, The University of Texas at Austin

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.utexas.wrap.net;

import edu.utexas.wrap.assignment.AssignmentContainer;
import edu.utexas.wrap.modechoice.Mode;

/**A tolled link whose travel time function is modeled using a BPR VDF
 * 
 * @author William
 *
 */
public class TolledBPRLink extends TolledLink {
	
	private final float b, power, toll;
	private final Double tp;

	
	public TolledBPRLink(Node tail, Node head, Float capacity, Float length, Float fftime, Float b, Float power, Float toll, Integer linkID) {
		super(tail,head,capacity,length,fftime, linkID);
		
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
		ttLock.readLock().lock();

		if (cachedTT == null) cachedTT = freeFlowTime()*(1+
				b*Math.pow(getFlow()/getCapacity(), power));
		double ret = cachedTT;
		ttLock.readLock().unlock();
		return ret;
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

	public double getPrice(AssignmentContainer container) {
		// TODO Auto-generated method stub
		return getPrice(container.valueOfTime(),container.vehicleClass());
	}
}
