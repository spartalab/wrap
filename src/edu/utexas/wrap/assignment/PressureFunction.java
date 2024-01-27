package edu.utexas.wrap.assignment;

import java.util.HashMap;
import java.util.Map;

import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.SignalGroup;
import edu.utexas.wrap.net.SignalizedNode;
import edu.utexas.wrap.net.TurningMovement;

public interface PressureFunction {

	
	public default double perVehicleDelay(Link link) {
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
					Math.exp(perVehicleDelay(mvmt))
					).sum());
	}	

	public double perVehicleDelay(TurningMovement mvmt);
	
	public Double delayPrime(TurningMovement mvmt, 
			double greenSharePrime, double cycleLengthPrime);


	public default double turningMovementPressure(TurningMovement tm) {
		// TODO Auto-generated method stub
		return tm.getTail().getCapacity()*perVehicleDelay(tm.getTail());
	}

	public default double signalGroupPressure(SignalGroup sigGroup) {
		return sigGroup.getLinks().stream()
		.mapToDouble(link -> perVehicleDelay(link)*link.getCapacity()).sum();
	}
	
	public default Map<SignalGroup, Double> getGreenShareChange(
			Graph network, Double scalingFactor
			) {
		Map<SignalGroup,Double> deltaG = new HashMap<SignalGroup,Double>();
		network.getNodes().stream()
		.filter(node -> node instanceof SignalizedNode)
		.map(node -> (SignalizedNode) node)
		.forEach(node -> {
	
			for (SignalGroup sg_a : node.getSignalGroups()) {
				for (SignalGroup sg_b : node.getSignalGroups()) {
					if (sg_a.getID() >= sg_b.getID()) continue;
					else {
	
						double pressureDiff = signalGroupPressure(
								sg_a
								) - signalGroupPressure(
										sg_b
										);
						deltaG.put(sg_a, 
								deltaG.getOrDefault(sg_a, 0.)
								+ scalingFactor*pressureDiff*node.getGreenShare(sg_a));
						deltaG.put(sg_b, 
								deltaG.getOrDefault(sg_b, 0.)
								- scalingFactor*pressureDiff*node.getGreenShare(sg_b));
					}
				}
			}
	
		});
		return deltaG;
	}

	public default Map<TurningMovement, Double> getGreenSplitChange(
			Graph network, double scalingFactor){
		Map<TurningMovement,Double> deltaM = new HashMap<TurningMovement,Double>();
		
		network.getNodes().stream()
		.filter(node -> node instanceof SignalizedNode)
		.map(node -> (SignalizedNode) node)
		.flatMap(node -> node.getSignalGroups().stream())
		.flatMap(sg -> sg.getRings())
		.forEach(ring -> {
			for (TurningMovement tm_a : ring.getTurningMovements()) {
				deltaM.putIfAbsent(tm_a, 0.);
				for (TurningMovement tm_b : ring.getTurningMovements()) {
					if (tm_a.getID() >= tm_b.getID()) continue;
					double pressureDiff = turningMovementPressure(
							tm_a
							) - turningMovementPressure(tm_b);
					
					deltaM.put(tm_a, deltaM.getOrDefault(tm_a, 0.)
							+ scalingFactor * pressureDiff*ring.getGreenShare(tm_a));
					deltaM.put(tm_b, deltaM.getOrDefault(tm_b, 0.)
							- scalingFactor*pressureDiff*ring.getGreenShare(tm_b));
				}
			}
		});
		;
		
		return deltaM;
	}

}
