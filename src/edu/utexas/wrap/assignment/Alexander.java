package edu.utexas.wrap.assignment;

import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.SignalGroup;
import edu.utexas.wrap.net.SignalizedNode;
import edu.utexas.wrap.net.TurningMovement;

public class Alexander implements PressureFunction {

	@Override
	public double signalGroupPressure(SignalGroup sigGroup) {
		return sigGroup.getLinks().stream()
		.mapToDouble(link -> perVehicleDelay(link)*link.getCapacity()).sum();
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
							Math.pow(1.-node.getGreenShare(mvmt),2)
							*node.getCycleLength()
							/2.
							)
					).sum());
	}

	@Override
	public Double delayPrime(
			TurningMovement mvmt, 
			double greenSharePrime, 
			double cycleLengthPrime) {
		// TODO Auto-generated method stub
		SignalizedNode intx = (SignalizedNode) mvmt.getTail().getHead();
		double numerator = Math.exp(
				Math.pow(1.-intx.getGreenShare(mvmt),2)*intx.getCycleLength()/2.
				);
		double denominator = intx.getMovements(mvmt.getTail())
				.stream().mapToDouble(j -> 
				Math.exp(
						Math.pow(1.-intx.getGreenShare(j), 2)*intx.getCycleLength()/2.)
				).sum();
		return (numerator/denominator)*(
				(1-intx.getGreenShare(mvmt))
				*(
						(1-intx.getGreenShare(mvmt))*cycleLengthPrime 
						- (2*intx.getCycleLength()*greenSharePrime))
				)/2;
	}

	@Override
	public double turningMovementPressure(TurningMovement tm) {
		// TODO Auto-generated method stub
		return tm.getTail().getCapacity()*perVehicleDelay(tm.getTail());
	}

}
