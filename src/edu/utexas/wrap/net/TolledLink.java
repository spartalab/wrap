package edu.utexas.wrap.net;

import edu.utexas.wrap.modechoice.Mode;

public abstract class TolledLink extends Link {
	

	public TolledLink(Node tail, Node head, Float capacity, Float length, Float fftime) {
		super(tail, head, capacity, length, fftime);
	}

	public abstract Float getToll(Mode c);
	
	public abstract double tollPrime();
}

