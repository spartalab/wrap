package edu.utexas.wrap.assignment;


import edu.utexas.wrap.net.SignalizedNode;
import edu.utexas.wrap.net.TurningMovement;

public class WLYM implements PressureFunction {
	private final double vcLimit = 0.9;

	@Override
	public double perVehicleDelay(TurningMovement tm) {
		double flow = tm.getTail().getFlow();
		double cap = tm.getTail().getCapacity();
		double vc = flow / cap;
		
		if (vc > vcLimit ) {
			return delayFunc(tm,cap*vcLimit) 
					+ (flow-(cap*vcLimit))*delayPrime(tm,0,0);
		}

		else return delayFunc(tm, flow);
		// address possible divide-by-zero or negative vals
	}

	private double delayFunc(TurningMovement tm, double flow) {
		SignalizedNode head = (SignalizedNode) tm.getTail().getHead();
		return flow*(1-head.getGreenShare(tm))
				*head.getCycleLength()
						/(tm.getTail().getCapacity() - flow);
	}
	
	public Double delayPrime(TurningMovement tm,
			double greenSharePrime, 
			double cycleLengthPrime) {

		SignalizedNode head = (SignalizedNode) tm.getTail().getHead();
		double capacity = tm.getTail().getCapacity();
		double flow = Math.min(
				tm.getTail().getFlow(),
				capacity * vcLimit);
		
		return (head.getCycleLength() * (
						flow*(flow-capacity)*greenSharePrime 
						+ capacity*(1-head.getGreenShare(tm))
					)
				)
				/ Math.pow(capacity - flow, 2);
	}
	

}
