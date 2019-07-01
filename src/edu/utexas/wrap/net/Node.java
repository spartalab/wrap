package edu.utexas.wrap.net;

public class Node {

	private final int ID;
	private final int graphOrder;
	private final boolean isCentroid;
	
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
}
