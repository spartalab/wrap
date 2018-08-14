package edu.utexas.wrap;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Map;

/**
 * @author SYSTEM
 *
 */
public class TolledEnhancedLink extends TolledLink {
	private final BigDecimal conicalParam, VDFshift, saturatedFlowRate, minDelay, operCost;
	private final BigDecimal a, b, c, d, s, u;
	private final Map<VehicleClass, Boolean> allowedClasses;
	private final Map<VehicleClass, BigDecimal> classTolls;
	
	
	public TolledEnhancedLink(	Node tail,
												Node head,
												Double capacity,
												Double length,
												Double fftime,
												BigDecimal conicalParam,
												BigDecimal VDFShift,
												BigDecimal sParam,
												BigDecimal uParam,
												BigDecimal saturatedFlowRate,
												BigDecimal minDelay,
												BigDecimal operCost,
												BigDecimal CA,
												BigDecimal CB,
												BigDecimal CC,
												BigDecimal CD,
												Map<VehicleClass, Boolean> allowedClasses,
												Map<VehicleClass, BigDecimal> classTolls) {
		super(tail, head, capacity, length, fftime);
		this.conicalParam = conicalParam;
		this.VDFshift = VDFShift;
		this.s = sParam;
		this.u = uParam;
		this.saturatedFlowRate = saturatedFlowRate;
		this.minDelay = minDelay;
		this.operCost = operCost;
		this.a = CA;
		this.b = CB;
		this.c = CC;
		this.d = CD;
		this.allowedClasses = allowedClasses;
		this.classTolls = classTolls;
	}

	@Override
	public BigDecimal getPrice(Double vot, VehicleClass c) {
		return BigDecimal.valueOf(getToll(c)).add(getTravelTime().multiply(BigDecimal.valueOf(vot),Optimizer.defMC));
	}

	@Override
	public BigDecimal pricePrime(Double vot) {
		// 	d( k(v) + m*t(v) )/dv
		//	==	t'(v) * m + k'(v)
		return tPrime().multiply(BigDecimal.valueOf(vot), Optimizer.defMC).add(tollPrime());
	}

	@Override
	public Double getToll(VehicleClass c) {
		return classTolls.get(c).add(operCost).doubleValue();
	}

	@Override
	public BigDecimal tollPrime() {
		return BigDecimal.ZERO;
	}

	@Override
	public Boolean allowsClass(VehicleClass c) {
		return allowedClasses.getOrDefault(c, true);
	}
	
	
	
	
	
	@Override
	public BigDecimal getTravelTime() {
		//	T == T_0 + c(v) + s(v) + u(v)
		return (cachedTT != null)? cachedTT : BigDecimal.valueOf(freeFlowTime()).add(conicalDelay()).add(signalDelay()).add(unsignalizedDelay());
	}

	
	private BigDecimal unsignalizedDelay() {
		//	u(v) == m + u*v/c
		return minDelay.add(
				u.multiply(getFlow()).divide(BigDecimal.valueOf(getCapacity()), Optimizer.defMC)
				);
	}

	private BigDecimal signalDelay() {
		//	S(v) == s / max(0.1, 1 - v/s)
		return s.divide(
				BigDecimal.valueOf(0.1).max(
						BigDecimal.ONE.subtract(	getFlow().divide(saturatedFlowRate, Optimizer.defMC)	)
						),
				Optimizer.defMC);
	}

	private BigDecimal conicalDelay() {
		//	c(v) == T_0 * (h(v) - h(0))
		BigDecimal x = getFlow().divide(BigDecimal.valueOf(getCapacity()), Optimizer.defMC);
		return BigDecimal.valueOf(freeFlowTime()).multiply(
				h(x).subtract(h(BigDecimal.ZERO)),
				Optimizer.defMC);
	}

	private BigDecimal h(BigDecimal x) {
		//	h(x) == 1 + r(x) - (a * g(x)) - b
		return BigDecimal.ONE
				.add(r(x))
				.subtract(conicalParam.multiply(g(x),Optimizer.defMC))
				.subtract(beta());
	}

	private BigDecimal r(BigDecimal x) {
		//	r(x) == sqrt( a^2 * g(x)^2 + b^2 )
		return conicalParam.pow(2).multiply(g(x).pow(2))
		.add(beta().pow(2))
		.sqrt(Optimizer.defMC);
	}
	
	private BigDecimal g(BigDecimal x) {
		//	g(x) == 1 + e - x
		return BigDecimal.ONE.subtract(x).add(VDFshift);
	}

	private BigDecimal beta() {
		//	b == ( 2*a - 1 )/( 2*a - 2 )
		return (conicalParam.multiply(BigDecimal.valueOf(2)).subtract(BigDecimal.ONE))
				.divide(conicalParam.multiply(BigDecimal.valueOf(2)).subtract(BigDecimal.valueOf(2)), Optimizer.defMC);
	}
	
	
	
	
	
	@Override
	public BigDecimal tPrime() {
		//	t'(v) == d( T_0 + c(v) + s(v) + u(v) )/dv
		//	==	c'(v) + s'(v) + u'(v)
		return conicalPrime().add(signalPrime()).add(unsignalPrime());
	}

	
	private BigDecimal unsignalPrime() {
		//	d'(v) == d( m + u*v/c )/dv
		//	==	u/c
		return u.divide(BigDecimal.valueOf(getCapacity()), Optimizer.defMC);
	}

	private BigDecimal conicalPrime() {
		//	c'(v) == d( T_0 * (h(v) - h(0))/dv
		//	==	T_0 * h'(v)
		return BigDecimal.valueOf(freeFlowTime()).multiply(hPrime(),Optimizer.defMC);
	}

	private BigDecimal hPrime() {
		//	h'(v) == d( 1 + r(v) - (a * g(v)) - b )/dv
		//	==	r'(v) - a * g'(v)
		return rPrime().subtract(conicalParam.multiply(gPrime(),Optimizer.defMC));
	}
	
	private BigDecimal rPrime() {
		//	r'(x) == d( sqrt( a^2 * g(x)^2 + b^2 ) )/dx
		//	==	a^2 * g(x) * g'(x) / sqrt( a^2 * g(x)^2 + b^2 ) 
		BigDecimal x = getFlow().divide(BigDecimal.valueOf(getCapacity()),Optimizer.defMC);
		return conicalParam.pow(2).multiply(g(x)).multiply(gPrime()).divide(r(x),Optimizer.defMC);
	}

	private BigDecimal gPrime() {
		//	g'(v) == d( 1 + e - v/c )/dv
		//	==	- 1/c
		return BigDecimal.ONE.divide(BigDecimal.valueOf(getCapacity()), Optimizer.defMC).negate();
	}

	private BigDecimal signalPrime() {
		// TODO: Explore boundary cases (0.875 and 0.925) where not differentiable
		
		// 	u = v/m
		BigDecimal vOverS = getFlow().divide(saturatedFlowRate, Optimizer.defMC);

		//	S'(v) == 
		
		//	If u <= 0.875:
		//		d( s/(1 - v/m) )/dv
		//		==	m*s / (m-v)^2
		if (vOverS.compareTo(BigDecimal.valueOf(0.875)) <= 0) {
			return saturatedFlowRate.multiply(s)
					.divide(saturatedFlowRate.subtract(getFlow())
							.pow(2),
							Optimizer.defMC);
		}
		
		//	Else if u >= 0.925:
		//		d( s/0.1 )/dv == 0
		else if (vOverS.compareTo(BigDecimal.valueOf(0.925)) >= 0) {
			return BigDecimal.ZERO;
		}

		//	Else: (i.e. 0.925 > u > 0.875)
		//		d( a*(v/m)^3 + b*(v/m)^2 + c*(v/m) + d )/dv
		//		==	((3 * a * v^2) + (2 * b * m * v) + (c * m^2)) / m^3
		else {
			return BigDecimal.valueOf(3).multiply(a).multiply(getFlow().pow(2))
					.add(BigDecimal.valueOf(2).multiply(b).multiply(saturatedFlowRate).multiply(getFlow()))
					.add(c.multiply(saturatedFlowRate.pow(2)))
					.divide(saturatedFlowRate.pow(3), Optimizer.defMC);
		}
	}
	
	
	
	
	
	@Override
	public BigDecimal tIntegral() {
		
		// 	Integral from 0 to v of:
		//
		//		(T_0 + c(x) + s(x) + u(x) ) dx
		//
		//		== 	Integral of T_0  dx
		//		+	Integral of c(x) dx
		//		+	Integral of s(x) dx
		//		+	Integral of u(x) dx
		
		return getFlow().compareTo(BigDecimal.ZERO) == 0? BigDecimal.ZERO : //Short circuit for zero-flow
			BigDecimal.valueOf(freeFlowTime()).multiply(getFlow(), Optimizer.defMC)	//Integral of T_0 dx == T_0 v
			.add(conicalIntegral())			//Integral of c(x) dx
			.add(signalizedIntegral())		//Integral of s(x) dx
			.add(unsignalizedIntegral());	//Integral of u(x) dx
	}

	
	private BigDecimal unsignalizedIntegral() {
		
		//	Integral from 0 to v of u(x) dx ==
		//	Integral from 0 to v of:
		//
		//		(m + u*x/c) dx
		//
		//		== 	m*v
		//		+	u*v^2/(2*c)
		
		return u.multiply(getFlow().pow(2)).divide(BigDecimal.valueOf(2*getCapacity()), Optimizer.defMC)	// u*v^2/(2*c)
				.add(minDelay.multiply(getFlow(), Optimizer.defMC)); // + m*v
	}

	private BigDecimal signalizedIntegral() {
		

		// u = v/m
		BigDecimal u = getFlow().divide(saturatedFlowRate, Optimizer.defMC);
		
		//	Integral from 0 to v of s(x) dx ==

		
		//	If u <= 0.875:
		//		From WolframAlpha
		//		Integral from 0 to v of:
		//		
		//			s/(1-u) dx
		//
		//			==	-m * s * ln(1 - v/m)
		//
		//	NOTE: Assume m >= v TODO: Ask about validity
		
		if (u.compareTo(BigDecimal.valueOf(0.875))<=0) {
			return s.negate().multiply(saturatedFlowRate).multiply(
					// natural log kludge, TODO: solve exactly
					BigDecimal.valueOf(
							Math.log(
									(BigDecimal.ONE.subtract(u)).doubleValue()
									)
							),
					Optimizer.defMC);
		} 
		
		//	Else if u >= 0.925:
		//		Integral from 0 to v of:
		//
		//			s/0.1
		//
		//			== 10*s*v
		
		else if (u.compareTo(BigDecimal.valueOf(0.925))>=0) {
			//	10 * s * v
			return BigDecimal.TEN.multiply(s).multiply(getFlow());
		} 
		
		//	Else: (i.e. 0.925 > u > 0.875)
		//		Integral from 0 to v of:
		//
		//			(a * u^3) + (b * u^2) + (c * u) + d
		//
		//			==	a * v^4 / (4 * m^3)
		//			+	b * v^3 / (3 * m^2)
		//			+	c * v^2 / (2 * m)
		//			+	d * v
		
		else {
			BigDecimal v = getFlow();
			BigDecimal m = saturatedFlowRate;
			
			
			BigDecimal firstTerm = a.multiply(v.pow(4)).divide(BigDecimal.valueOf(4).multiply(m.pow(3)), Optimizer.defMC);	// a * v^4 / (4 * m^3)
			BigDecimal secondTerm = b.multiply(v.pow(3)).divide(BigDecimal.valueOf(3).multiply(m.pow(2)), Optimizer.defMC);	// b * v^3 / (3 * m^2)
			BigDecimal thirdTerm = c.multiply(v.pow(2)).divide(BigDecimal.valueOf(2).multiply(m), Optimizer.defMC);			// c * v^2 / (2 * m)
			return firstTerm.add(secondTerm).add(thirdTerm).add(d.multiply(v));								// d * v
		}
	}

	private BigDecimal conicalIntegral() {
		
		//	Integral from 0 to v of:
		//
		//		(f*h(x) - f*h(0)) dx
		//
		//	==	f * Integral of h(x) dx 
		// 	-	f * v * h(0)
		
		BigDecimal v = getFlow();
		BigDecimal f = BigDecimal.valueOf(freeFlowTime());
		
		return f.multiply(hIntegral().subtract(v.multiply(h(BigDecimal.ZERO))),Optimizer.defMC);
	}

	private BigDecimal hIntegral() {
		//	Integral from 0 to v of h(x) dx ==

		// Integral from 0 to v of:
		//
		//		( 1 - b + r(x) - (a * g(x)) ) dx
		//
		//	==	v * (1 - b) 
		//	+	Integral of r(x) dx
		//	-	a * Integral of g(x) dx
		
		return getFlow().multiply(BigDecimal.ONE.subtract(beta()))
				.add(rIntegral())
				.subtract(conicalParam.multiply(gIntegral()),Optimizer.defMC);
	}

	private BigDecimal gIntegral() {
		//	Integral from 0 to v of g(x) dx ==

		//	Integral from 0 to v of:
		//
		//		(1 - x/c + e) dx
		//
		//		== -(v^2)/(2*c) + v + 1
		
		BigDecimal v = getFlow();

		return v.pow(2).divide(BigDecimal.valueOf(2*getCapacity()), Optimizer.defMC).negate()	// -(v^2)/(2*c)
				.add(v)	// + v + 1
				.add(BigDecimal.ONE);
	}

	private BigDecimal rIntegral() {
		// From Integral Calculator
		//	Integral from 0 to v of r(x) dx ==

		// Integral from 0 to v of:
		//
		//		sqrt(
		//			(a^2 * (h - x/c)^2) + b^2
		//		)
		//
		//	==	(	b^2 * c^2 * arsinh( a*(v-c*h)/(b*c) )
		//		+	a * (v-c*h) * sqrt(	a^2 * v^2
		//							-	2 * c * h * a^2 * v
		//							+	(b^2 * c^2) + (h^2 * a^2 * c^2)
		//							)
		//		+	b^2 * c^2 * arsinh( h*a/b )
		//		+	h * a * c * sqrt( (b^2 * c^2)  + (h^2 * a^2 * c^2) )
		//		)
		//		/	(2*a*c)
		
		// NOTE: assume a, b != 0
		
		BigDecimal a = conicalParam;
		BigDecimal b = beta();
		BigDecimal c = BigDecimal.valueOf(getCapacity());
		BigDecimal h = BigDecimal.ONE.add(VDFshift);
		BigDecimal v = getFlow();
		
		// sqrt( (b^2 * c^2) + (h^2 * a^2 * c^2) )
		BigDecimal smallRootTerm = a.pow(2).multiply(h.pow(2)).multiply(c.pow(2))
				.add(b.pow(2).multiply(c.pow(2)))
				.sqrt(MathContext.DECIMAL64);
		
		// sqrt(	(a^2 * v^2) - (2 * c * h * a^2 * v) + (h^2 * a^2 * c^2) + (b^2 * c^2)  )
		BigDecimal bigRootTerm = a.pow(2).multiply(v.pow(2))
				.subtract(BigDecimal.valueOf(2).multiply(a.pow(2)).multiply(h).multiply(v).multiply(c))
				.add(a.pow(2).multiply(h.pow(2)).multiply(c.pow(2)))
				.add(b.pow(2).multiply(c.pow(2)))
				.sqrt(MathContext.DECIMAL64);
		
		
		BigDecimal firstTerm = b.pow(2).multiply(c.pow(2)).multiply(arsinh(a.multiply(v.subtract(c.multiply(h))).divide(b.multiply(c), Optimizer.defMC)));
		BigDecimal secondTerm = a.multiply(v.subtract(h.multiply(c))).multiply(bigRootTerm);
		BigDecimal thirdTerm = b.pow(2).multiply(c.pow(2)).multiply(arsinh(a.multiply(h).divide(b, Optimizer.defMC)));
		BigDecimal fourthTerm = a.multiply(h).multiply(c).multiply(smallRootTerm);
		
		return firstTerm.add(secondTerm).add(thirdTerm).add(fourthTerm)
				.divide(a.multiply(c).multiply(BigDecimal.valueOf(2)), Optimizer.defMC);
	}

	private BigDecimal arsinh(BigDecimal x) {
		
		//	From Wikipedia
		//	arsinh(x) == ln( x + sqrt(x^2 + 1) )
		
		 // natural log kludge, TODO: solve exactly
		return BigDecimal.valueOf(
				Math.log(		// ln (
						x.doubleValue() +	// x +  
						x.pow(2).add(BigDecimal.ONE).sqrt(MathContext.DECIMAL64).doubleValue() // sqrt( x^2 + 1 )
						)
				);
	}

}