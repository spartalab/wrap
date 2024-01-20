package edu.utexas.wrap.net;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SignalizedNode extends Node {
	

	private Float cycleLength;
	private Collection<SignalGroup> sigGroups;
	private Map<Link,SignalGroup> linkSGMap;
	private Float[] groupGreenShares;
	private Map<Integer,Map<Integer,TurningMovement>> unlinkedMovements;
	private int numMovements;


	public SignalizedNode(
			Integer ID, 
			Integer order, 
			TravelSurveyZone zone
			) {
		super(ID, order, zone);
		numMovements = 0;
		unlinkedMovements = new HashMap<Integer,Map<Integer,TurningMovement>>();
	}
	
	public void setSignalGroups(
			Collection<SignalGroup> signalGroups,
			Map<Link,SignalGroup> linkMapper) {
		sigGroups = signalGroups;
		linkSGMap = linkMapper;
		groupGreenShares = new Float[signalGroups.size()];
	}
	
	public Double getGreenShare(TurningMovement tm) {
		if (tm instanceof LinkedTurningMovement) 
			return linkedGreenShare((LinkedTurningMovement) tm);
		SignalGroup sg = getSignalGroup(tm.getTail());
		return groupGreenShares[sg.getOrder()] * sg.getGreenShare(tm);
	}
	
	public Float getGreenShare(SignalGroup sg) {
		// TODO Auto-generated method stub
		return groupGreenShares[sg.getOrder()];
	}
	
	private Double linkedGreenShare(LinkedTurningMovement tm) {
		return tm.getParents().stream()
				.mapToDouble(otm -> getGreenShare(otm)).sum();
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
		sigGroups.stream().forEach(entry -> {
			groupGreenShares[entry.getID()] += deltaG.get(entry).floatValue();
		});
		setGroupGreenShares(shares);
	}

	public Collection<TurningMovement> getMovements(Link inLink){
		return linkSGMap.get(inLink).getMovements(inLink);
	}

	public void updateMovementGreenShares(Map<TurningMovement, Double> deltaM) {
		// TODO Auto-generated method stub
		sigGroups.stream().forEach(sg -> sg.updateMovementGreenShares(deltaM));
	}
	
	public TurningMovement newTurningMovement(Link inLink, Link outLink) {
		TurningMovement tm = new TurningMovement(inLink, outLink, numMovements++);
		unlinkedMovements.putIfAbsent(inLink.hashCode(), new HashMap<Integer,TurningMovement>());
		unlinkedMovements.get(inLink.hashCode()).put(outLink.hashCode(), tm);
		return tm;
	}
	
	public LinkedTurningMovement newLinkedTurningMovement(Link inLink, Link outLink, Integer[] params) {
		Set<TurningMovement> compats = new HashSet<TurningMovement>();
		compats.add(unlinkedMovements.get(inLink.hashCode()).get(params[0]));
		compats.add(unlinkedMovements.get(params[1]).get(params[2]));
		return new LinkedTurningMovement(inLink,outLink,numMovements++,compats);

	}

}
