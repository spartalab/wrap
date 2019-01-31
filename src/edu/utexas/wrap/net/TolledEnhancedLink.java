package edu.utexas.wrap.net;

import java.util.Map;

import edu.utexas.wrap.modechoice.Mode;

/**
 * @author SYSTEM
 *
 */
public class TolledEnhancedLink extends TolledLink {
	private final float conicalParam, VDFshift, saturatedFlowRate, minDelay, operCost;
	private final float a, b, c, d, s, u;
	private final Map<Mode, Boolean> allowedClasses;
	private final Map<Mode, Float> classTolls;

	public TolledEnhancedLink(Node tail, Node head, Float capacity, Float length, Float fftime, Float conicalParam,
			Float VDFShift, Float sParam, Float uParam, Float saturatedFlowRate, Float minDelay, Float operCost,
			Float CA, Float CB, Float CC, Float CD, Map<Mode, Boolean> allowedClasses,
			Map<Mode, Float> classTolls) {
		super(tail, head, capacity, length, fftime);
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
		this.allowedClasses = allowedClasses;
		this.classTolls = classTolls;
	}

	@Override
	public Boolean allowsClass(Mode c) {
		return allowedClasses.getOrDefault(c, true);
	}

	private Double arsinh(Double x) {

		// From Wikipedia
		// arsinh(x) == ln( x + sqrt(x^2 + 1) )

		return Math.log( // ln (
				x + Math.sqrt(Math.pow(x, 2) + 1)// sqrt( x^2 + 1 )
		);
	}

	private Double beta() {
		// b == ( 2*a - 1 )/( 2*a - 2 )
		return ((double) conicalParam * 2 - 1) / (conicalParam * 2 - 2);
	}

	private Double conicalDelay() {
		// c(v) == T_0 * (h(v) - h(0))
		Double x = getFlow() / getCapacity();
		return freeFlowTime() * (h(x) - h(0.0));
	}

	private Double conicalIntegral() {

		// Integral from 0 to v of:
		//
		// (f*h(x) - f*h(0)) dx
		//
		// == f * Integral of h(x) dx
		// - f * v * h(0)

		Double f = (double) freeFlowTime();

		return f * (hIntegral() - (getFlow() * h(0.0)));
	}

	private Double conicalPrime() {
		// c'(v) == d( T_0 * (h(v) - h(0))/dv
		// == T_0 * h'(v)
		return freeFlowTime() * hPrime();
	}

	private Double g(Double x) {
		// g(x) == 1 + e - x
		return 1 - x + VDFshift;
	}

	@Override
	public Double getPrice(Float vot, Mode c) {
		return getToll(c) + getTravelTime() * vot;
	}

	@Override
	public Float getToll(Mode c) {
		if (!allowsClass(c))
			return Float.MAX_VALUE;
		return classTolls.get(c) + operCost;
	}

	@Override
	public Double getTravelTime() {
		// T == T_0 + c(v) + s(v) + u(v)
		return (cachedTT != null) ? cachedTT : freeFlowTime() + conicalDelay() + signalDelay() + unsignalizedDelay();
	}

	private Double gIntegral() {
		// Integral from 0 to v of g(x) dx ==

		// Integral from 0 to v of:
		//
		// (1 - x/c + e) dx
		//
		// == -(v^2)/(2*c) + v + 1

		Double v = getFlow();

		return -Math.pow(v, 2) / (2 * getCapacity()) // -(v^2)/(2*c)
				+ v + 1;
	}

	private Double gPrime() {
		// g'(v) == d( 1 + e - v/c )/dv
		// == - 1/c
		return -1.0 / getCapacity();
	}

	private Double h(Double x) {
		// h(x) == 1 + r(x) - (a * g(x)) - b
		return 1 + r(x) - (conicalParam * g(x)) - beta();
	}

	private Double hIntegral() {
		// Integral from 0 to v of h(x) dx ==

		// Integral from 0 to v of:
		//
		// ( 1 - b + r(x) - (a * g(x)) ) dx
		//
		// == v * (1 - b)
		// + Integral of r(x) dx
		// - a * Integral of g(x) dx

		return getFlow() * (1 - beta()) + rIntegral() - conicalParam * gIntegral();
	}

	private Double hPrime() {
		// h'(v) == d( 1 + r(v) - (a * g(v)) - b )/dv
		// == r'(v) - a * g'(v)
		return rPrime() - conicalParam * gPrime();
	}

	@Override
	public Double pricePrime(Float vot) {
		// d( k(v) + m*t(v) )/dv
		// == t'(v) * m + k'(v)
		return tPrime() * vot + tollPrime();
	}

	private Double r(Double x) {
		// r(x) == sqrt( a^2 * g(x)^2 + b^2 )
		return Math.sqrt(Math.pow(conicalParam, 2) * Math.pow(g(x), 2) + Math.pow(beta(), 2));
	}

	private Double rIntegral() {
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

		Double a = (double) conicalParam;
		Double b = beta();
		Double c = (double) getCapacity();
		Double h = 1.0 + VDFshift;
		Double v = getFlow();

		// sqrt( (b^2 * c^2) + (h^2 * a^2 * c^2) )
		Double smallRootTerm = Math
				.sqrt(Math.pow(a, 2) * Math.pow(h, 2) * Math.pow(c, 2) + (Math.pow(b, 2) * Math.pow(c, 2)));

		// sqrt( (a^2 * v^2) - (2 * c * h * a^2 * v) + (h^2 * a^2 * c^2) + (b^2 * c^2) )
		Double bigRootTerm = Math.sqrt(Math.pow(a, 2) * Math.pow(v, 2) - (2.0 * Math.pow(a, 2) * h * v * c)
				+ (Math.pow(a, 2) * Math.pow(h, 2) * Math.pow(c, 2)) + (Math.pow(b, 2) * Math.pow(c, 2)));

		Double firstTerm = Math.pow(b, 2) * Math.pow(c, 2) * arsinh(a * (v - (c * h)) / (b * c));
		Double secondTerm = a * (v - (h * c)) * bigRootTerm;
		Double thirdTerm = Math.pow(b, 2) * Math.pow(c, 2) * arsinh(a * h / b);
		Double fourthTerm = a * h * c * smallRootTerm;

		return (firstTerm + secondTerm + thirdTerm + fourthTerm) / (a * c * 2.0);
	}

	private Double rPrime() {
		// r'(x) == d( sqrt( a^2 * g(x)^2 + b^2 ) )/dx
		// == a^2 * g(x) * g'(x) / sqrt( a^2 * g(x)^2 + b^2 )
		Double x = getFlow() / getCapacity();
		return Math.pow(conicalParam, 2) * g(x) * gPrime() / r(x);
	}

	private Double signalDelay() {
		// S(v) == s / max(0.1, 1 - v/s)
		return s / Math.max(0.1, 1 - (getFlow() / saturatedFlowRate));
	}

	private Double signalizedIntegral() {

		// u = v/m
		Double u = getFlow() / saturatedFlowRate;

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
			Double v = getFlow();

			Double firstTerm = a * Math.pow(v, 4) / (4 * Math.pow(saturatedFlowRate, 3)); // a * v^4 / (4 * m^3)
			Double secondTerm = b * Math.pow(v, 3) / (3 * Math.pow(saturatedFlowRate, 2)); // b * v^3 / (3 * m^2)
			Double thirdTerm = c * Math.pow(v, 2) / (2 * saturatedFlowRate); // c * v^2 / (2 * m)
			return firstTerm + secondTerm + thirdTerm + (d * v); // d * v
		}
	}

	private Double signalPrime() {
		// TODO: Explore boundary cases (0.875 and 0.925) where not differentiable

		// u = v/m
		Double vOverS = getFlow() / saturatedFlowRate;

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

	@Override
	public Double tIntegral() {

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

	@Override
	public Double tollPrime() {
		return 0.0;
	}

	@Override
	public Double tPrime() {
		// t'(v) == d( T_0 + c(v) + s(v) + u(v) )/dv
		// == c'(v) + s'(v) + u'(v)
		return conicalPrime() + signalPrime() + unsignalPrime();
	}

	private Double unsignalizedDelay() {
		// u(v) == m + u*v/c
		return minDelay + u * getFlow() / getCapacity();
	}

	private Double unsignalizedIntegral() {

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

	private Double unsignalPrime() {
		// d'(v) == d( m + u*v/c )/dv
		// == u/c
		return ((double) u) / getCapacity();
	}

}