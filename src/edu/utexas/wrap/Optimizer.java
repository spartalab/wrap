package edu.utexas.wrap;

public abstract class Optimizer {

	protected final Network network;
	
	public Optimizer(Network network) {
		this.network = network;
	}

	public Network getNetwork() {
		return network;
	}

	public abstract void optimize() throws Exception;
		
	public abstract String toString();
		
}
