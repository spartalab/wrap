package edu.utexas.wrap.assignment;

import edu.utexas.wrap.net.SignalizedNode;
import edu.utexas.wrap.net.TurningMovement;

/**
 * An extension of {@link Alexander} that adds an oversaturation delay
 * component to the uniform delay. When demand exceeds capacity, a queue
 * builds over multiple signal cycles, and the additional delay incurred
 * by vehicles waiting in that queue is estimated. The number of cycles
 * over which oversaturation accumulates is configurable (default: 100
 * cycles, representing approximately 2.5 hours at typical cycle lengths).
 *
 * @author Will Alexander
 */
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
		
		if (uniformDelay + oversaturationDelay > 700) System.out.println((
				mvmt.getTail().getFlow() - mvmt.getTail().getCapacity()
			));
		return uniformDelay + oversaturationDelay;
		
		
//		throw new RuntimeException("not yet implemented");
	}

	@Override
	public Double delayPrime(TurningMovement mvmt, double greenSharePrime, double cycleLengthPrime) {
		// TODO Auto-generated method stub
		
		double uniformPrime = super.delayPrime(mvmt, greenSharePrime, cycleLengthPrime);
		if (!(mvmt.getTail().getHead() instanceof SignalizedNode)) return uniformPrime;
		
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
