package edu.utexas.wrap;

public class CentroidConnector extends TolledLink {
	private Float toll;

	public CentroidConnector(Node tail, Node head, Float capacity, Float length, Float fftime, Float toll) {
		super(tail, head, capacity, length, fftime);
		this.toll = toll;
	}

	@Override
	public Double getTravelTime() {
		return (double) freeFlowTime();
	}

	@Override
	public Double tPrime() {
		return 0.0;
	}

	@Override
	public Double tIntegral() {
		return freeFlowTime()*getFlow();
	}

	@Override
	public Double getPrice(Float vot, VehicleClass c) {
		return (double) (vot*freeFlowTime() + toll);
	}

	@Override
	public Double pricePrime(Float vot) {
		return 0.0;
	}

	@Override
	public Boolean allowsClass(VehicleClass c) {
		return true;
	}

	@Override
	public Float getToll(VehicleClass c) {
		if (!allowsClass(c)) return Float.MAX_VALUE;
		return toll;
	}

	@Override
	public Double tollPrime() {
		return 0.0;
	}

}
