package edu.utexas.wrap.net;

public class Node {

	private final int ID;
	private final TravelSurveyZone zone;
	private final int graphOrder;
	private Link[] outLinks, inLinks;
	
	public Node(Integer ID, Integer order, TravelSurveyZone zone) {
		this.ID = ID;
		graphOrder = order;
		this.zone = zone;
	}
	
	public Node(Node n) {
		this.ID = n.ID;
		this.graphOrder = n.graphOrder;
		this.zone = n.zone;
	}
	
	public boolean equals(Node n) {
		return n.getID() == this.ID;
	}

	public Integer getID() {
		return ID;
	}

	public int hashCode() {
		return ID;
	}


	public boolean isCentroid() {
		return zone != null;
	}


	public String toString() {
		return "Node " + this.ID;
	}
	
	public int getOrder() {
		return graphOrder;
	}

	public void setForwardStar(Link[] fs) {
		// TODO Auto-generated method stub
		outLinks = fs;
	}

	public void setReverseStar(Link[] rs) {
		// TODO Auto-generated method stub
		inLinks = rs;
	}
	
	public Link[] forwardStar() {
		return outLinks == null? new Link[0] : outLinks;
	}
	
	public Link[] reverseStar() {
		return inLinks;
	}

	public int orderOf(Link l) {
		// TODO Auto-generated method stub
		return l.headIndex();
	}

	public TravelSurveyZone getZone() {
		return zone;
	}

}
