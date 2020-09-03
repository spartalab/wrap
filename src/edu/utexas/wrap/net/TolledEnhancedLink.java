package edu.utexas.wrap.net;

import edu.utexas.wrap.assignment.AssignmentContainer;
import edu.utexas.wrap.modechoice.Mode;

/**
 * @author SYSTEM
 *
 */
public class TolledEnhancedLink extends TolledLink {
	private final float conicalParam, VDFshift, saturatedFlowRate, minDelay, operCost;
	private final float a, b, c, d, s, u;
	private final double beta, h0, betaSquared, conicalSquared;
	private final boolean[] classesAllowed;
	private final float[] classTolls;

	public TolledEnhancedLink(Node tail, Node head, Float capacity, Float length, Float fftime, Float conicalParam,
			Float VDFShift, Float sParam, Float uParam, Float saturatedFlowRate, Float minDelay, Float operCost,
			Float CA, Float CB, Float CC, Float CD, 
			float[] classTolls, boolean[] classesAllowed, Integer linkID) {
		super(tail, head, capacity, length, fftime, linkID);
		this.conicalParam = conicalParam;
		this.VDFshift = VDFShift;
		s = sParam;
		u = uParam;
		this.saturatedFlowRate = saturatedFlowRate;
		this.minDelay = minDelay;
		this.operCost = operCost;
		a = CA;
		b = CB;
		c = CC;
		d = CD;
		this.classesAllowed = classesAllowed;
		this.classTolls = classTolls;
		beta = ((double) conicalParam * 2 - 1) / (conicalParam * 2 - 2);
		betaSquared = Math.pow(beta, 2);
		conicalSquared = Math.pow(conicalParam, 2);
		h0 = h(0.0);
	}

	public Boolean allowsClass(Mode c) {
		return classesAllowed[c.ordinal()];
//		return allowedClasses.getOrDefault(c, true);
	}

	private double arsinh(Double x) {

		// From Wikipedia
		// arsinh(x) == ln( x + sqrt(x^2 + 1) )

		return Math.log( // ln (
				x + Math.sqrt(Math.pow(x, 2) + 1)// sqrt( x^2 + 1 )
		);
	}

	private double beta() {
		// b == ( 2*a - 1 )/( 2*a - 2 )
		return beta;
	}

	private double conicalDelay() {
		// c(v) == T_0 * (h(v) - h(0))
		Double x = getFlow() / getCapacity();
		return freeFlowTime() * (h(x) - h0);
	}

	private double conicalIntegral() {

		// Integral from 0 to v of:
		//
		// (f*h(x) - f*h(0)) dx
		//
		// == f * Integral of h(x) dx
		// - f * v * h(0)

		double f = (double) freeFlowTime();

		return f * (hIntegral() - (getFlow() * h0));
	}

	private double conicalPrime() {
		// c'(v) == d( T_0 * (h(v) - h(0))/dv
		// == T_0 * h'(v)
		return freeFlowTime() * hPrime();
	}

	private double g(Double x) {
		// g(x) == 1 + e - x
		return 1 - x + VDFshift;
	}

	public double getPrice(Float vot, Mode c) {
		return getToll(c) + getTravelTime() * vot;
	}

	public Float getToll(Mode c) {
		if (!allowsClass(c))
			return Float.MAX_VALUE;
		return classTolls[c.ordinal()] + operCost;
	}

	public synchronized double getTravelTime() {
		// T == T_0 + c(v) + s(v) + u(v)
		try {
			ttSem.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (cachedTT == null) cachedTT = freeFlowTime() + conicalDelay() + signalDelay() + unsignalizedDelay();	
		double ret = cachedTT;
		ttSem.release();
		return ret;
	}

	private double gIntegral() {
		// Integral from 0 to v of g(x) dx ==

		// Integral from 0 to v of:
		//
		// (1 - x/c + e) dx
		//
		// == -(v^2)/(2*c) + v + 1

		double v = getFlow();

		return -Math.pow(v, 2) / (2 * getCapacity()) // -(v^2)/(2*c)
				+ v + 1;
	}

	private double gPrime() {
		// g'(v) == d( 1 + e - v/c )/dv
		// == - 1/c
		return -1.0 / getCapacity();
	}

	private double h(Double x) {
		// h(x) == 1 + r(x) - (a * g(x)) - b
		return 1 + r(x) - (conicalParam * g(x)) - beta;
	}

	private double hIntegral() {
		// Integral from 0 to v of h(x) dx ==

		// Integral from 0 to v of:
		//
		// ( 1 - b + r(x) - (a * g(x)) ) dx
		//
		// == v * (1 - b)
		// + Integral of r(x) dx
		// - a * Integral of g(x) dx

		return getFlow() * (1 - beta) + rIntegral() - conicalParam * gIntegral();
	}

	private double hPrime() {
		// h'(v) == d( 1 + r(v) - (a * g(v)) - b )/dv
		// == r'(v) - a * g'(v)
		return rPrime() - conicalParam * gPrime();
	}

	public double pricePrime(Float vot) {
		// d( k(v) + m*t(v) )/dv
		// == t'(v) * m + k'(v)
		return tPrime() * vot + tollPrime();
	}

	private double r(Double x) {
		// r(x) == sqrt( a^2 * g(x)^2 + b^2 )
		return Math.sqrt(conicalSquared * Math.pow(g(x), 2) + betaSquared);
	}

	private double rIntegral() {
		// From Integral Calculator
		// Integral from 0 to v of r(x) dx ==

		// Integral from 0 to v of:
		//
		// sqrt(
		// (a^2 * (h - x/c)^2) + b^2
		// )
		//
		// == ( b^2 * c^2 * arsinh( a*(v-c*h)/(b*c) )
		// + a * (v-c*h) * sqrt( a^2 * v^2
		// - 2 * c * h * a^2 * v
		// + (b^2 * c^2) + (h^2 * a^2 * c^2)
		// )
		// + b^2 * c^2 * arsinh( h*a/b )
		// + h * a * c * sqrt( (b^2 * c^2) + (h^2 * a^2 * c^2) )
		// )
		// / (2*a*c)

		// NOTE: assume a, b != 0

		double a = (double) conicalParam;
		double b = beta();
		double c = (double) getCapacity();
		double h = 1.0 + VDFshift;
		double v = getFlow();

		// sqrt( (b^2 * c^2) + (h^2 * a^2 * c^2) )
		double smallRootTerm = Math
				.sqrt(Math.pow(a, 2) * Math.pow(h, 2) * Math.pow(c, 2) + (Math.pow(b, 2) * Math.pow(c, 2)));

		// sqrt( (a^2 * v^2) - (2 * c * h * a^2 * v) + (h^2 * a^2 * c^2) + (b^2 * c^2) )
		double bigRootTerm = Math.sqrt(Math.pow(a, 2) * Math.pow(v, 2) - (2.0 * Math.pow(a, 2) * h * v * c)
				+ (Math.pow(a, 2) * Math.pow(h, 2) * Math.pow(c, 2)) + (Math.pow(b, 2) * Math.pow(c, 2)));

		double firstTerm = Math.pow(b, 2) * Math.pow(c, 2) * arsinh(a * (v - (c * h)) / (b * c));
		double secondTerm = a * (v - (h * c)) * bigRootTerm;
		double thirdTerm = Math.pow(b, 2) * Math.pow(c, 2) * arsinh(a * h / b);
		double fourthTerm = a * h * c * smallRootTerm;

		return (firstTerm + secondTerm + thirdTerm + fourthTerm) / (a * c * 2.0);
	}

	private double rPrime() {
		// r'(x) == d( sqrt( a^2 * g(x)^2 + b^2 ) )/dx
		// == a^2 * g(x) * g'(x) / sqrt( a^2 * g(x)^2 + b^2 )
		double x = getFlow() / getCapacity();
		return Math.pow(conicalParam, 2) * g(x) * gPrime() / r(x);
	}

	private double signalDelay() {
		// S(v) == s / max(0.1, 1 - v/s)
		return s / Math.max(0.1, 1 - (getFlow() / saturatedFlowRate));
	}

	private double signalizedIntegral() {

		// u = v/m
		double u = getFlow() / saturatedFlowRate;

		// Integral from 0 to v of s(x) dx ==

		// If u <= 0.875:
		// From WolframAlpha
		// Integral from 0 to v of:
		//
		// s/(1-u) dx
		//
		// == -m * s * ln(1 - v/m)
		//
		// NOTE: Assume m >= v TODO: Ask about validity

		if (u <= 0.875) {
			return -s * saturatedFlowRate * Math.log(1 - u);
		}

		// Else if u >= 0.925:
		// Integral from 0 to v of:
		//
		// s/0.1
		//
		// == 10*s*v

		else if (u >= 0.925) {
			// 10 * s * v
			return 10 * s * getFlow();
		}

		// Else: (i.e. 0.925 > u > 0.875)
		// Integral from 0 to v of:
		//
		// (a * u^3) + (b * u^2) + (c * u) + d
		//
		// == a * v^4 / (4 * m^3)
		// + b * v^3 / (3 * m^2)
		// + c * v^2 / (2 * m)
		// + d * v

		else {
			double v = getFlow();

			double firstTerm = a * Math.pow(v, 4) / (4 * Math.pow(saturatedFlowRate, 3)); // a * v^4 / (4 * m^3)
			double secondTerm = b * Math.pow(v, 3) / (3 * Math.pow(saturatedFlowRate, 2)); // b * v^3 / (3 * m^2)
			double thirdTerm = c * Math.pow(v, 2) / (2 * saturatedFlowRate); // c * v^2 / (2 * m)
			return firstTerm + secondTerm + thirdTerm + (d * v); // d * v
		}
	}

	private double signalPrime() {
		// TODO: Explore boundary cases (0.875 and 0.925) where not differentiable

		// u = v/m
		double vOverS = getFlow() / saturatedFlowRate;

		// S'(v) ==

		// If u <= 0.875:
		// d( s/(1 - v/m) )/dv
		// == m*s / (m-v)^2
		if (vOverS <= 0.875)
			return (saturatedFlowRate * s) / Math.pow((saturatedFlowRate - getFlow()), 2);

		// Else if u >= 0.925:
		// d( s/0.1 )/dv == 0
		else if (vOverS >= 0.925)
			return 0.0;

		// Else: (i.e. 0.925 > u > 0.875)
		// d( a*(v/m)^3 + b*(v/m)^2 + c*(v/m) + d )/dv
		// == ((3 * a * v^2) + (2 * b * m * v) + (c * m^2)) / m^3
		else
			return (3 * a * Math.pow(getFlow(), 2)) + (2 * b * saturatedFlowRate * getFlow())
					+ (c * Math.pow(saturatedFlowRate, 2) / Math.pow(saturatedFlowRate, 3));

	}

	public double tIntegral() {

		// Integral from 0 to v of:
		//
		// (T_0 + c(x) + s(x) + u(x) ) dx
		//
		// == Integral of T_0 dx
		// + Integral of c(x) dx
		// + Integral of s(x) dx
		// + Integral of u(x) dx

		return getFlow() == 0 ? 0.0 : // Short circuit for zero-flow
				(freeFlowTime() * getFlow()) // Integral of T_0 dx == T_0 v
						+ (conicalIntegral()) // Integral of c(x) dx
						+ (signalizedIntegral()) // Integral of s(x) dx
						+ (unsignalizedIntegral()); // Integral of u(x) dx
	}

	public double tollPrime() {
		return 0.0;
	}

	public double tPrime() {
		// t'(v) == d( T_0 + c(v) + s(v) + u(v) )/dv
		// == c'(v) + s'(v) + u'(v)
		return conicalPrime() + signalPrime() + unsignalPrime();
	}

	private double unsignalizedDelay() {
		// u(v) == m + u*v/c
		return minDelay + u * getFlow() / getCapacity();
	}

	private double unsignalizedIntegral() {

		// Integral from 0 to v of u(x) dx ==
		// Integral from 0 to v of:
		//
		// (m + u*x/c) dx
		//
		// == m*v
		// + u*v^2/(2*c)

		return u * Math.pow(getFlow(), 2) / (2 * getCapacity()) // u*v^2/(2*c)
				+ (minDelay * getFlow()); // + m*v
	}

	private double unsignalPrime() {
		// d'(v) == d( m + u*v/c )/dv
		// == u/c
		return ((double) u) / getCapacity();
	}

	public double getPrice(AssignmentContainer container) {
		return getPrice(container.valueOfTime(),container.vehicleClass());
	}

}