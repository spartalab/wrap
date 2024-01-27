package edu.utexas.wrap.assignment;

import edu.utexas.wrap.net.SignalizedNode;
import edu.utexas.wrap.net.TurningMovement;

public class P0 implements PressureFunction {

	@Override
	public double perVehicleDelay(TurningMovement tm) {
		if (!(tm.getTail().getHead() instanceof SignalizedNode)) return 0;

		SignalizedNode node = (SignalizedNode) tm.getTail().getHead();
		
		return tm.getTail().getFlow() /
				(tm.getTail().getCapacity()*node.getGreenShare(tm));
	}

	@Override
	public Double delayPrime(TurningMovement mvmt, double greenSharePrime, double cycleLengthPrime) {

		SignalizedNode node = (SignalizedNode) mvmt.getTail().getHead();
		Double flow = mvmt.getTail().getFlow();
		
		return (node.getGreenShare(mvmt) - flow*greenSharePrime)/
				(
						mvmt.getTail().getCapacity() *
						Math.pow(node.getGreenShare(mvmt),2)
				);
	}

	
}
