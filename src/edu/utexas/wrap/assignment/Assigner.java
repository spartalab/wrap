package edu.utexas.wrap.assignment;

import java.util.function.ToDoubleFunction;

import edu.utexas.wrap.demand.ODProfile;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.NetworkSkim;

public interface Assigner extends Runnable {
	
	public void process(ODProfile profile);
	
	public NetworkSkim getSkim(ToDoubleFunction<Link> function);
	
}
