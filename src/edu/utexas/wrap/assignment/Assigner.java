package edu.utexas.wrap.assignment;

import edu.utexas.wrap.demand.ODProfile;

public interface Assigner extends Runnable {
	
	public void process(ODProfile profile);
}
