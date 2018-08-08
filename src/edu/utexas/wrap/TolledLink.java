package edu.utexas.wrap;

import java.math.BigDecimal;

public abstract class TolledLink extends Link {
	
	protected Double toll;

	public TolledLink(Node tail, Node head, Double capacity, Double length, Double fftime, Double toll) {
		// TODO Auto-generated constructor stub
		super(tail, head, capacity, length, fftime);
		this.toll = toll;
	}

	public abstract Double getToll();
	
	public abstract BigDecimal tollPrime();
}
