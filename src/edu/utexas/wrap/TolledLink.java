package edu.utexas.wrap;

import java.math.BigDecimal;

public abstract class TolledLink extends Link {
	

	public TolledLink(Node tail, Node head, Float capacity, Float length, Float fftime) {
		super(tail, head, capacity, length, fftime);
	}

	public abstract Float getToll(VehicleClass c);
	
	public abstract BigDecimal tollPrime();
}

