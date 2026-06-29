package edu.utexas.wrap.assignment;

import edu.utexas.wrap.net.SignalizedNode;
import edu.utexas.wrap.net.TurningMovement;

/**
 * A {@link PressureFunction} based on the uniform delay component of the
 * Highway Capacity Manual delay formula. The per-vehicle delay is computed as
 * {@code (1 - g)^2 * C / 2}, where {@code g} is the effective green share and
 * {@code C} is the cycle length. This represents the average delay experienced
 * by a vehicle arriving uniformly during a signal cycle.
 *
 * @author Will Alexander
 */
public class Alexander implements PressureFunction {

	
	@Override
	public double perVehicleDelay(TurningMovement mvmt) {
		if (!(mvmt.getTail().getHead() instanceof SignalizedNode)) return 0.;
		SignalizedNode node = (SignalizedNode) mvmt.getTail().getHead();

		return 
				Math.pow(1.-node.getGreenShare(mvmt),2)
				*node.getCycleLength()/2.;
	}

	@Override
	public Double delayPrime(
			TurningMovement mvmt, 
			double greenSharePrime, 
			double cycleLengthPrime) {
		// TODO Auto-generated method stub
		SignalizedNode intx = (SignalizedNode) mvmt.getTail().getHead();
		double numerator = perVehicleDelay(mvmt);
		double denominator = intx.getMovements(mvmt.getTail())
				.stream().mapToDouble(this::perVehicleDelay).sum();
		return (numerator/denominator)*(
				(1-intx.getGreenShare(mvmt))
				*(
						(1-intx.getGreenShare(mvmt))*cycleLengthPrime 
						- (2*intx.getCycleLength()*greenSharePrime))
				)/2;
	}

}


