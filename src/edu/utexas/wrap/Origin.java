package edu.utexas.wrap;

import java.util.HashMap;

public class Origin extends Node{
	private Bush bush;
	private int[] demandVector;
	
//	public Origin(List<Link> incomingLinks, List<Link> outgoingLinks, Bush bush, int[] demandVector) {
//		super(incomingLinks, outgoingLinks);
//		this.bush = bush;
//		this.demandVector = demandVector;
//	}

	public Origin(Node self, HashMap<Integer, Double> dests) {
		// TODO Auto-generated constructor stub - How to handle 
		super(self.getIncomingLinks(), self.getOutgoingLinks(), self.getID());
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
