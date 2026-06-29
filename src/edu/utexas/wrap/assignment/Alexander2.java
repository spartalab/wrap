package edu.utexas.wrap.assignment;

import edu.utexas.wrap.net.TurningMovement;

/**
 * An extension of {@link Alexander} that weights the uniform delay by the
 * volume-to-capacity ratio of the upstream link. This produces higher
 * pressure values for movements that are more heavily loaded relative
 * to their capacity, encouraging green time reallocation toward
 * congested approaches.
 *
 * @author Will Alexander
 */
public class Alexander2 extends Alexander implements PressureFunction {
	@Override
	public double perVehicleDelay(TurningMovement mvmt) {
		return super.perVehicleDelay(mvmt) * mvmt.getTail().getFlow() / mvmt.getTail().getCapacity();
	}
}
