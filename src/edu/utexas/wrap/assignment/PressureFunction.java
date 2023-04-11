package edu.utexas.wrap.assignment;

import edu.utexas.wrap.net.Link;

public interface PressureFunction {

	public double stagePressure(Link link);
	
	public double perVehicleDelay(Link link);

}
