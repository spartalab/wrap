package edu.utexas.wrap.assignment;

import edu.utexas.wrap.net.Link;

public class P0 implements PressureFunction {

	@Override
	public double stagePressure(Link link) {
		// TODO Auto-generated method stub
		return link.getCapacity() * getBottleneckDelay(link);
	}

	public double getBottleneckDelay(Link l) {
//		return getFlow()*(1-
//			(getHead() instanceof SignalizedNode?
//				((SignalizedNode) getHead())
//				.getGreenShare(this).doubleValue()
//				: 0.))
//				*( getHead() instanceof SignalizedNode?
//								((SignalizedNode) 
//										getHead()).getCycleLength()
//								: 0.
//						)/(getCapacity() - getFlow())
		
//	return (((1.-getGreenShare(inLink))*cycleLength)
//	*((1.-getGreenShare(inLink))*cycleLength + 1)
//	
//	/ (2.*cycleLength));
		
//		return getQueueLength()/(getCapacity()*getGreenShare());
		
		throw new RuntimeException("Not yet implemented")
				//TODO address possible divide-by-zero or negative vals
;
	}
	
}
