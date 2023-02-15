package edu.utexas.wrap.net;

public class SignalizedNode extends Node {
	
	private Float[] greenShares;
	private Float cycleLength;

	public SignalizedNode(
			Integer ID, 
			Integer order, 
			TravelSurveyZone zone) {
		super(ID, order, zone);
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
	
	public void setGreenShares(Float[] newShares) {
		greenShares = newShares;
	}
	
	public void setCycleLength(Float newCycLength) {
		cycleLength = newCycLength;
	}
	
	public double getSignalizedDelay(Link inLink) {
		return 
			
		((1.-getGreenShare(inLink))*cycleLength)
		*((1.-getGreenShare(inLink))*cycleLength + 1)
		
		/ (2.*cycleLength);
	}
}
