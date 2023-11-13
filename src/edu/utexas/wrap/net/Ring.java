package edu.utexas.wrap.net;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class Ring {
	
	//THESE RINGS DONT CROSS BARRIERS so they're more like subrings
	
	private Map<TurningMovement,Double> turningMvmtGreenShares;
	
	public Ring(Map<TurningMovement,Double> ringShares) {
		double sum = ringShares.values().stream()
				.mapToDouble(i -> i).sum();
		this.turningMvmtGreenShares = ringShares.entrySet().stream()
				.collect(
					Collectors.toMap(
						Map.Entry::getKey, 
						entry -> entry.getValue()/sum));
		
	}
	
	public Double getGreenShare(TurningMovement tm) {
		// TODO Auto-generated method stub
		return turningMvmtGreenShares.get(tm);
	}
	
	public void setGreenShares(Map<TurningMovement,Double> newShares) {
		this.turningMvmtGreenShares = newShares;
	}

	public Collection<TurningMovement> getTurningMovements(){
		return turningMvmtGreenShares.keySet();
	}
}
