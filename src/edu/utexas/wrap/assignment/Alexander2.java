package edu.utexas.wrap.assignment;

import edu.utexas.wrap.net.TurningMovement;

public class Alexander2 extends Alexander implements PressureFunction {
	@Override
	public double perVehicleDelay(TurningMovement mvmt) {
		return super.perVehicleDelay(mvmt) * mvmt.getTail().getFlow() / mvmt.getTail().getCapacity();
	}
}
