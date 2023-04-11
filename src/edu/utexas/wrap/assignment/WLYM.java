package edu.utexas.wrap.assignment;

import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.SignalizedNode;

public class WLYM implements PressureFunction {

	@Override
	public double perVehicleDelay(Link link) {
		// TODO Auto-generated method stub

		return link.getFlow()*(1-
			(link.getHead() instanceof SignalizedNode?
				((SignalizedNode) link.getHead())
				.getGreenShare(link).doubleValue()
				: 0.))
				*( link.getHead() instanceof SignalizedNode?
								((SignalizedNode) 
										link.getHead()).getCycleLength()
								: 0.
						)/(link.getCapacity() - link.getFlow());
		//TODO address possible divide-by-zero or negative vals
	}
	
	public double stagePressure(Link link) {
		return perVehicleDelay(link) * link.getCapacity();
	}

}
