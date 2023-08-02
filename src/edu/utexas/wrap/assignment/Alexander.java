package edu.utexas.wrap.assignment;

import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.SignalizedNode;
import edu.utexas.wrap.net.TurningMovement;

public class Alexander implements PressureFunction {

	@Override
	public double stagePressure(Link link) {
		
		
		return perVehicleDelay(link)*link.getFlow() ;
			
	}
	
	public double perVehicleDelay(Link link) {
		if (!(link.getHead() instanceof SignalizedNode)) return 0;

		SignalizedNode node = (SignalizedNode) link.getHead();
		
		/*for each turning movement from the link,
		 * evaluate the per-vehicle delay for that movement
		 * then take the logsumexp to get a smooth maximum
		 * (this assumes spillback blocks the other movements)
		 * */
		return Math.log(node.getMovements(link).stream()
			.mapToDouble(
					mvmt -> 
					Math.exp(
							(1.-node.getGreenShare(mvmt))
							*node.getCycleLength()
							/2.
							)
					).sum());
	}

	@Override
	public Double delayPrime(Link link) {
		// TODO Auto-generated method stub

		return 0.;
	}

}
