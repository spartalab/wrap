package edu.utexas.wrap.assignment;

import edu.utexas.wrap.net.SignalizedNode;
import edu.utexas.wrap.net.TurningMovement;

public class Alexander3 extends Alexander implements PressureFunction {

	
	private int numCycles = 100; //150 minutes = 2.5 hours
	
	@Override
	public double perVehicleDelay(TurningMovement mvmt) {
		// TODO Auto-generated method stub
		if (!(mvmt.getTail().getHead() instanceof SignalizedNode)) return 0.;
		SignalizedNode head = (SignalizedNode) mvmt.getTail().getHead();
		
		double uniformDelay = super.perVehicleDelay(mvmt);
		
		double queueLength = numCycles * head.getCycleLength() * (
					mvmt.getTail().getFlow() - mvmt.getTail().getCapacity()
				);
		
		queueLength = queueLength > 0? queueLength : 0;
		
		double numCycles = queueLength / mvmt.getTail().getCapacity();
		
		double oversaturationDelay = numCycles * head.getCycleLength() / 2;
		
		return uniformDelay + oversaturationDelay;
		
		
//		throw new RuntimeException("not yet implemented");
	}

	@Override
	public Double delayPrime(TurningMovement mvmt, double greenSharePrime, double cycleLengthPrime) {
		// TODO Auto-generated method stub
		
		double uniformPrime = super.delayPrime(mvmt, greenSharePrime, cycleLengthPrime);
		
		SignalizedNode node = (SignalizedNode) mvmt.getTail().getHead();
		
		double flow = mvmt.getTail().getFlow(),
				cycleLength = node.getCycleLength(),
				capacity = mvmt.getTail().getCapacity();
		
		
		double oversaturationPrime = flow < capacity? 0 :
				
				numCycles * cycleLength * (flow - capacity) * cycleLengthPrime
				+
				numCycles * Math.pow(cycleLength, 2);
		
		return uniformPrime + oversaturationPrime;
//		throw new RuntimeException("not yet implemented");
	}

}
