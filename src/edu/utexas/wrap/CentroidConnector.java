package edu.utexas.wrap;

import java.math.BigDecimal;

public class CentroidConnector extends TolledLink {
	private Double toll;

	public CentroidConnector(Node tail, Node head, Double capacity, Double length, Double fftime, Double toll) {
		super(tail, head, capacity, length, fftime);
		this.toll = toll;
	}

	@Override
	public BigDecimal getTravelTime() {
		return BigDecimal.valueOf(freeFlowTime());
	}

	@Override
	public BigDecimal tPrime() {
		return BigDecimal.ZERO;
	}

	@Override
	public BigDecimal tIntegral() {
		return BigDecimal.valueOf(freeFlowTime()).multiply(getFlow(), Optimizer.defMC);
	}

	@Override
	public BigDecimal getPrice(Double vot, VehicleClass c) {
		return BigDecimal.valueOf(vot*freeFlowTime() + toll);
	}

	@Override
	public BigDecimal pricePrime(Double vot) {
		return BigDecimal.ZERO;
	}

	@Override
	public Boolean allowsClass(VehicleClass c) {
		return true;
	}

	@Override
	public Double getToll(VehicleClass c) {
		return toll;
	}

	@Override
	public BigDecimal tollPrime() {
		return BigDecimal.ZERO;
	}

}
