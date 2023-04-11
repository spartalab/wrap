package edu.utexas.wrap.assignment;

import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.SignalizedNode;

public class Alexander implements PressureFunction {

	@Override
	public double stagePressure(Link link) {
		// TODO Auto-generated method stub
		
		
		return perVehicleDelay(link)*link.getCapacity() ;
			
	}
	
	public double perVehicleDelay(Link link) {
//		return 0.;
		if (!(link.getHead() instanceof SignalizedNode)) return 0;

		SignalizedNode node = (SignalizedNode) link.getHead();

		return (1.-node.getGreenShare(link))*node.getCycleLength()/2 ;
	}

}
