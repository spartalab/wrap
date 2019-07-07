package edu.utexas.wrap.net;

import java.util.stream.IntStream;

public class Node {

	private final int ID;
	private final int graphOrder;
	private final boolean isCentroid;
	private Link[] outLinks, inLinks;
	
	public Node(Integer ID, Boolean isCentroid, Integer order) {
		this.ID = ID;
		this.isCentroid = isCentroid;
		graphOrder = order;
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
		return isCentroid;
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
		return IntStream.range(0,inLinks.length).filter(x -> inLinks[x].equals(l)).findAny().orElse(-1);
	}
}
