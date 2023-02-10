package edu.utexas.wrap.assignment;

public class MarginalVOTPolicy {
	private double vot;
	private boolean useMarginalCost;
	
	public MarginalVOTPolicy(boolean enabled) {
		useMarginalCost = enabled;
	}
	
	public double getVOT() {
		return vot;
	};
	
	public boolean useMarginalCost() {
		return useMarginalCost;
	}

	public void setMarginalVOT(Double vot) {
		this.vot = vot;
	}
}
