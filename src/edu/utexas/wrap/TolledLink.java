package edu.utexas.wrap;

import java.math.BigDecimal;

import org.mapdb.DB;

public abstract class TolledLink extends Link {
	

	public TolledLink(DB db, Node tail, Node head, Float capacity, Float length, Float fftime) {
		super(db, tail, head, capacity, length, fftime);
	}

	public abstract Float getToll(VehicleClass c);
	
	public abstract BigDecimal tollPrime();
}

