package edu.utexas.wrap.assignment;

import edu.utexas.wrap.net.SignalizedNode;
import edu.utexas.wrap.net.TurningMovement;

public class Alexander implements PressureFunction {

	
	@Override
	public double perVehicleDelay(TurningMovement mvmt) {
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


