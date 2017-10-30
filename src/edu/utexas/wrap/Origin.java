package edu.utexas.wrap;

import java.util.List;

public class Origin extends Node{
	private Bush bush;
	private int[] demandVector;
	
	public Origin(List<Link> incomingLinks, List<Link> outgoingLinks, Bush bush, int[] demandVector) {
		super(incomingLinks, outgoingLinks);
		this.bush = bush;
		this.demandVector = demandVector;
	}

	public Bush getBush() {
		return bush;
	}

	public void setBush(Bush bush) {
		this.bush = bush;
	}

	public int[] getdemandVector() {
		return demandVector;
	}

	public void setdVector(int[] dVector) {
		this.demandVector = dVector;
	}
	
}
