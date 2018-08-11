package edu.utexas.wrap;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Map;

/**
 * @author SYSTEM
 *
 */
public class RestrictedClassDependentTollEnhancedLink extends TolledLink {
	private final BigDecimal conicalParam, VDFshift, saturatedFlowRate, minDelay, operCost;
	private final BigDecimal a, b, c, d, s, u;
	private final Map<VehicleClass, Boolean> allowedClasses;
	private final Map<VehicleClass, BigDecimal> classTolls;
	
	
	public RestrictedClassDependentTollEnhancedLink(	Node tail,
												Node head,
												Double capacity,
												Double length,
												Double fftime,
												BigDecimal conicalParam,
												BigDecimal VDFShift,
												BigDecimal sParam,
												BigDecimal uParam,
												BigDecimal saturatedFlowRate,
												BigDecimal unsigDelay,
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
		this.minDelay = unsigDelay;
		this.operCost = operCost;
		this.a = CA;
		this.b = CB;
		this.c = CC;
		this.d = CD;
		this.allowedClasses = allowedClasses;
		this.classTolls = classTolls;
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
	public BigDecimal getTravelTime() {
		return BigDecimal.valueOf(freeFlowTime()).add(conicalDelay()).add(signalDelay()).add(unsignalizedDelay());
	}

	private BigDecimal unsignalizedDelay() {
		return minDelay.add(
				u.multiply(getFlow()).divide(BigDecimal.valueOf(getCapacity()))
				);
	}

	private BigDecimal signalDelay() {
		// TODO Talk to Venky about this (honestly, we should do a code review for this whole class)
		return s.divide(
				BigDecimal.valueOf(0.1).max(
						BigDecimal.ONE.subtract(
								getFlow().divide(saturatedFlowRate)
								)
						));
	}

	private BigDecimal conicalDelay() {
		BigDecimal x = getFlow().divide(BigDecimal.valueOf(getCapacity()));
		return BigDecimal.valueOf(freeFlowTime()).multiply(
				h(x).subtract(h(BigDecimal.ZERO))
				);
	}

	private BigDecimal h(BigDecimal x) {
		return BigDecimal.ONE.add(
				// Square root of all this
				(
						conicalParam.pow(2)
						.multiply(conicalParentheticTerm(x).pow(2))
						.add(beta().pow(2))
						
						).sqrt(MathContext.DECIMAL64))

		.subtract(conicalParam.multiply(conicalParentheticTerm(x)))
		.subtract(beta());
	}
	
	private BigDecimal conicalParentheticTerm(BigDecimal x) {
		return BigDecimal.ONE.subtract(x).add(VDFshift);
	}

	private BigDecimal beta() {
		return (conicalParam.multiply(BigDecimal.valueOf(2)).subtract(BigDecimal.ONE))
				.divide((conicalParam.multiply(BigDecimal.valueOf(2)).subtract(BigDecimal.valueOf(2))));
	}
	
	@Override
	public BigDecimal tPrime() {
		return conicalPrime().add(signalPrime()).add(unsignalPrime());
	}

	private BigDecimal unsignalPrime() {
		return u.divide(BigDecimal.valueOf(getCapacity()));
	}

	private BigDecimal conicalPrime() {
		return BigDecimal.valueOf(freeFlowTime()).multiply(hPrime());
	}

	private BigDecimal hPrime() {
		return rootPrime().subtract(conicalParam.multiply(parentheticPrime()));
	}
	
	private BigDecimal rootPrime() {
		BigDecimal x = getFlow().divide(BigDecimal.valueOf(getCapacity()));
		return conicalParam.pow(2).multiply(conicalParentheticTerm(x)).multiply(parentheticPrime()).divide(
				(
						conicalParam.pow(2).multiply(conicalParentheticTerm(x).pow(2)).add(beta().pow(2)))
				.sqrt(MathContext.DECIMAL64)
				);
	}

	private BigDecimal parentheticPrime() {
		return BigDecimal.ONE.divide(BigDecimal.valueOf(getCapacity())).negate();
	}

	private BigDecimal signalPrime() {
		BigDecimal vOverS = getFlow().divide(saturatedFlowRate);
		if (vOverS.compareTo(BigDecimal.valueOf(0.875)) <= 0) {
			return saturatedFlowRate.multiply(s).divide(
					(saturatedFlowRate.subtract(getFlow())).pow(2)
					);
		} else if (vOverS.compareTo(BigDecimal.valueOf(0.925)) >= 0) {
			return BigDecimal.ZERO;
		} else {
			return (BigDecimal.valueOf(3).multiply(a).multiply(getFlow().pow(2)).add(
					BigDecimal.valueOf(2).multiply(b).multiply(saturatedFlowRate).multiply(getFlow())).add(
							c.multiply(saturatedFlowRate.pow(2)))).divide(
									saturatedFlowRate.pow(3));
		}
	}

	@Override
	public BigDecimal tIntegral() {
		return BigDecimal.valueOf(freeFlowTime()).multiply(getFlow()).add(
				conicalIntegral()).add(signalizedIntegral()).add(unsignalizedIntegral());
	}

	private BigDecimal unsignalizedIntegral() {
		return u.multiply(getFlow().pow(2)).divide(BigDecimal.valueOf(2*getCapacity()))
				
				.add(minDelay.multiply(getFlow()));
	}

	private BigDecimal signalizedIntegral() {
		BigDecimal vOverS = getFlow().divide(saturatedFlowRate);
		if (vOverS.compareTo(BigDecimal.valueOf(0.875))<=0) {
			return s.negate().multiply(saturatedFlowRate).multiply(
					// natural log kludge, TODO: solve exactly
					BigDecimal.valueOf(
							Math.log(
									(BigDecimal.ONE.subtract(vOverS)).doubleValue()
									)
							)
					);
		} else if (vOverS.compareTo(BigDecimal.valueOf(0.925))>=0) {
			return BigDecimal.TEN.multiply(s).multiply(getFlow());
		} else {
			BigDecimal v = getFlow();
			return v.pow(2).multiply(
					v.multiply(
							BigDecimal.valueOf(3).multiply(a).multiply(v).add(
									BigDecimal.valueOf(4).multiply(b).multiply(saturatedFlowRate)
									)
							).add(
									BigDecimal.valueOf(6).multiply(c).multiply(saturatedFlowRate.pow(2))
									)
					).divide(
							BigDecimal.valueOf(12).multiply(saturatedFlowRate.pow(3))
							).add(
									d.multiply(v)
									);
		}
	}

	private BigDecimal conicalIntegral() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BigDecimal getPrice(Double vot, VehicleClass c) {
		return BigDecimal.valueOf(getToll(c)).add(getTravelTime().multiply(BigDecimal.valueOf(vot)));
	}

	@Override
	public BigDecimal pricePrime(Double vot) {
		return tPrime().add(tollPrime());
	}

	@Override
	public Boolean allowsClass(VehicleClass c) {
		return allowedClasses.getOrDefault(c, true);
	}

}
