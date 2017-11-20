package edu.utexas.wrap;

public abstract class Optimizer {

	private Network network;
	
	public Optimizer(Network network) {
		this.network = network;
	}

	public Network getNetwork() {
		return network;
	}

	public void setNetwork(Network network) {
		this.network = network;
	}

	public abstract void optimize();
	
	public void getResults(){
		//TODO
	}
	
	
}
