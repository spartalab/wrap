package edu.utexas.wrap.assignment;

import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.SignalGroup;
import edu.utexas.wrap.net.TurningMovement;

public interface PressureFunction {

	public double signalGroupPressure(SignalGroup sigGroup);
	
	public double perVehicleDelay(Link link);

	public Double delayPrime(TurningMovement mvmt);

	public double turningMovementPressure(TurningMovement tm_a);

}
