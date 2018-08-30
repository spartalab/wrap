package edu.utexas.wrap;

import java.math.BigDecimal;

import org.mapdb.DB;

public class CentroidConnector extends TolledLink {
	private Float toll;

	public CentroidConnector(DB db, Node tail, Node head, Float capacity, Float length, Float fftime, Float toll) {
		super(db, tail, head, capacity, length, fftime);
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
	public BigDecimal getPrice(Float vot, VehicleClass c) {
		return BigDecimal.valueOf(vot*freeFlowTime() + toll);
	}

	@Override
	public BigDecimal pricePrime(Float vot) {
		return BigDecimal.ZERO;
	}

	@Override
	public Boolean allowsClass(VehicleClass c) {
		return true;
	}

	@Override
	public Float getToll(VehicleClass c) {
		return toll;
	}

	@Override
	public BigDecimal tollPrime() {
		return BigDecimal.ZERO;
	}

}
