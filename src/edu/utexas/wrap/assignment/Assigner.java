package edu.utexas.wrap.assignment;

import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.net.NetworkSkim;

public interface Assigner {
	
	public void assign();
	
	public void attach(ODMatrix matrix);
	
	public NetworkSkim getSkim();

}
