package edu.utexas.wrap.net;

import java.util.Collection;
import java.util.Map;

public class SignalizedNode extends Node {
	

	private Float cycleLength;
	private Collection<SignalGroup> sigGroups;
	private Map<Link,SignalGroup> linkSGMap;
	private Float[] groupGreenShares;


	public SignalizedNode(
			Integer ID, 
			Integer order, 
			TravelSurveyZone zone
			) {
		super(ID, order, zone);
	}
	
	public void setSignalGroups(
			Collection<SignalGroup> signalGroups,
			Map<Link,SignalGroup> linkMapper) {
		sigGroups = signalGroups;
		linkSGMap = linkMapper;
		groupGreenShares = new Float[signalGroups.size()];
	}
	
	public Double getGreenShare(TurningMovement tm) {
		SignalGroup sg = getSignalGroup(tm.getTail());
		return groupGreenShares[sg.getID()] * sg.getGreenShare(tm);
	}
	
	public Float getGreenShare(SignalGroup sg) {
		// TODO Auto-generated method stub
		return groupGreenShares[sg.getID()];
	}
		
	public SignalGroup getSignalGroup(Link l) {
		return linkSGMap.get(l);
	}
	
	public void setGroupGreenShares(Float[] newGroupShares) {
		groupGreenShares = newGroupShares;
	}
	
	public Float getCycleLength() {
		return cycleLength;
	}
	
	public void setCycleLength(Float newCycLength) {
		cycleLength = newCycLength;
	}
	
	public Collection<SignalGroup> getSignalGroups(){
		return sigGroups;
	}

	
	public void updateGroupGreenShares(Map<SignalGroup, Double> deltaG) {
		// TODO Auto-generated method stub
		Float[] shares = new Float[groupGreenShares.length];
//		deltaG.entrySet().stream().forEach(entry -> {
//			groupGreenShares[entry.getKey().getID()] += entry.getValue().floatValue();
//		});
		setGroupGreenShares(shares);
		throw new RuntimeException("Not yet implemented");
	}

	public Collection<TurningMovement> getMovements(Link inLink){
		return linkSGMap.get(inLink).getMovements(inLink);
	}
	

}
