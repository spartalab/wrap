package edu.utexas.wrap;

import java.util.HashMap;

public class Origin extends Node{
	private Bush bush;
	private HashMap<Integer, Double> destinations;

//	public Origin(List<Link> incomingLinks, List<Link> outgoingLinks, Bush bush, int[] demandVector) {
//		super(incomingLinks, outgoingLinks);
//		this.bush = bush;
//		this.demandVector = demandVector;
//	}

	public Origin(Node self, HashMap<Integer, Double> dests) {
		super(self.getIncomingLinks(), self.getOutgoingLinks(), self.getID());
		destinations = dests;	// store demand HashMap
		bush = createInitialBush();
	}

	private Bush createInitialBush() {
		// TODO: create initial bush using Dijkstra's shortest path algorithm (at free flow)
		return null;
	}

	public Bush getBush() {
		return bush;
	}

	public void setBush(Bush bush) {
		this.bush = bush;
	}


}
