package edu.utexas.wrap;

import java.math.BigDecimal;

public abstract class TolledLink extends Link {
	

	public TolledLink(Node tail, Node head, Double capacity, Double length, Double fftime) {
		super(tail, head, capacity, length, fftime);
	}

	public abstract Double getToll(VehicleClass c);
	
	public abstract BigDecimal tollPrime();
}

