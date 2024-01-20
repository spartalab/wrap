package edu.utexas.wrap.assignment;

import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.SignalizedNode;
import edu.utexas.wrap.net.TurningMovement;

public class WLYM implements PressureFunction {
	private final double vcLimit = 0.9;

	@Override
	public double perVehicleDelay(Link link) {
		// TODO Auto-generated method stub
		double flow = link.getFlow();
		double cap = link.getCapacity();
		double vc = flow / cap;
		
		if (vc > vcLimit ) {
			return delayFunc(link,cap*vcLimit) 
					+ (flow-(cap*vcLimit))*delayPrime(link,cap*vcLimit);
		}

		else return delayFunc(link, flow);
		//TODO address possible divide-by-zero or negative vals
	}

	private double delayFunc(Link link, double flow) {
		return flow*(1-
			(link.getHead() instanceof SignalizedNode?
				((SignalizedNode) link.getHead())
				.getGreenShare(link).doubleValue()
				: 0.))
				*( link.getHead() instanceof SignalizedNode?
								((SignalizedNode) 
										link.getHead()).getCycleLength()
								: 0.
						)/(link.getCapacity() - flow);
	}
	
	public double stagePressure(Link link) {
		return perVehicleDelay(link) * link.getCapacity();
	}
	
	@Override
	public Double delayPrime(TurningMovement tm,
			double greenSharePrime, 
			double cycleLengthPrime) {
		// TODO Auto-generated method stub
		if (l.getFlow() > l.getCapacity() * vcLimit)
			return delayPrime(l,l.getCapacity() * vcLimit);
		else return delayPrime(l,l.getFlow());
	}
}
