package edu.utexas.wrap.net;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents a signal ring (or sub-ring) within a signalized intersection.
 * A ring defines a set of turning movements that share green time within
 * a signal phase, with each movement receiving a proportional share of the
 * ring's allocated time. Rings do not cross barrier lines, so they represent
 * independent timing groups within the overall signal cycle.
 *
 * <p>Green share updates are applied proportionally: when the optimization
 * algorithm adjusts signal timings, the ring redistributes green time among
 * its constituent movements according to the computed pressure differentials.
 *
 * @author Will Alexander
 * @see SignalGroup
 * @see TurningMovement
 * @see edu.utexas.wrap.assignment.PressureFunction
 */
public class Ring {
	
	//THESE RINGS DONT CROSS BARRIERS so they're more like subrings
	
	private Map<TurningMovement,Double> turningMvmtGreenShares;
	private final Collection<TurningMovement> movements;
	private final int ringID;
	
	public Ring(Map<TurningMovement,Double> ringShares, int ringID) {
		double sum = ringShares.values().stream()
				.mapToDouble(i -> i).sum();
		this.turningMvmtGreenShares = ringShares.entrySet().stream()
				.collect(
					Collectors.toMap(
						Map.Entry::getKey, 
						entry -> entry.getValue()/sum));
		this.movements = ringShares.keySet();
		this.ringID = ringID;
		
	}
	
	public Double getGreenShare(TurningMovement tm) {
		// TODO Auto-generated method stub
		return turningMvmtGreenShares.get(tm);
	}
	
	public String toString() {
		return "Ring "+Integer.toString(ringID);
	}

	public Collection<TurningMovement> getTurningMovements(){
		return turningMvmtGreenShares.keySet();
	}

	public void updateGreenShares(Map<TurningMovement, Double> deltaM) {
		// TODO Auto-generated method stub
		double sum = movements.stream()
				.mapToDouble(mvmt -> deltaM.getOrDefault(mvmt,0.)).sum();
		turningMvmtGreenShares = movements.stream().collect(
				Collectors.toMap(Function.identity(), 
						mvmt->deltaM.getOrDefault(mvmt,0.)/sum)
				);
	}

}
