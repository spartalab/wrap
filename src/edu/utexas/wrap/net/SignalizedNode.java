package edu.utexas.wrap.net;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

public class SignalizedNode extends Node {
	
	private Float[] greenShares;
	private Float cycleLength;
//	private final Map<Integer, Collection<Integer>> compatiblePhases;


	public SignalizedNode(
			Integer ID, 
			Integer order, 
			TravelSurveyZone zone,
			Map<Integer, Collection<Integer>> compatiblePhases) {
		super(ID, order, zone);
//		this.compatiblePhases = compatiblePhases;
	}
	
	@Override
	public void setReverseStar(Link[] rs) {
		super.setReverseStar(rs);
		greenShares = new Float[rs.length];
	}
	
	public Float getGreenShare(Link l) {
		return greenShares[l.headIndex()];
	}
	
	public Float getCycleLength() {
		return cycleLength;
	}
	
	protected void setGreenShares(Float[] newShares) {
		greenShares = newShares;
	}
	
	public void setCycleLength(Float newCycLength) {
		cycleLength = newCycLength;
	}

//	public boolean compatiblePhases(Link i, Link j) {
//		
//		return compatiblePhases.get(i.hashCode()).contains(j.hashCode()) ||
//				compatiblePhases.get(j.hashCode()).contains(i.hashCode());
//	}

	public void updateGreenShares(Map<Link, Double> deltaG) {
		// TODO Auto-generated method stub
		Float[] shares = new Float[greenShares.length];
		Stream.of(reverseStar()).forEach(l -> {
				shares[l.headIndex()] = (float) 
						(deltaG.get(l) + greenShares[l.headIndex()] );
			
		});
		setGreenShares(shares);
	}
	
	

}
