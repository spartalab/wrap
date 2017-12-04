package edu.utexas.wrap;

import java.util.List;

public abstract class Optimizer {

	protected Network network;
	
	public Optimizer(Network network) {
		this.network = network;
	}

	public Network getNetwork() {
		return network;
	}

	public void setNetwork(Network network) {
		this.network = network;
	}

	public abstract void optimize() throws Exception;
	
	public abstract List<Double> getResults() throws Exception;
	
	public abstract String toString();
		
}
