package edu.utexas.wrap.assignment;

import edu.utexas.wrap.net.SignalizedNode;
import edu.utexas.wrap.net.TurningMovement;

public class P0 implements PressureFunction {

	@Override
	public double perVehicleDelay(TurningMovement tm) {
		if (!(tm.getTail().getHead() instanceof SignalizedNode)) return 0;

		SignalizedNode node = (SignalizedNode) tm.getTail().getHead();
		
		return tm.getTail().getFlow() /
				(tm.getTail().getCapacity()*node.getGreenShare(tm)*node.getCycleLength());
	}

	@Override
	public Double delayPrime(TurningMovement mvmt, double greenSharePrime, double cycleLengthPrime) {

		SignalizedNode node = (SignalizedNode) mvmt.getTail().getHead();
		Double flow = mvmt.getTail().getFlow();
		
		double numerator = perVehicleDelay(mvmt);
		double denominator = node.getMovements(mvmt.getTail())
				.stream().mapToDouble(this::perVehicleDelay).sum();
		return (numerator/denominator)*(node.getGreenShare(mvmt) - flow*greenSharePrime)/
				(
						mvmt.getTail().getCapacity() * node.getCycleLength() *
						Math.pow(node.getGreenShare(mvmt),2)
				);
	}

	
}
