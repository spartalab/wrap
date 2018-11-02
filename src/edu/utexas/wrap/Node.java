package edu.utexas.wrap;

public class Node {

	private final Integer ID;
	private final Boolean isCentroid;
	
	public Node(Integer ID, Boolean isCentroid) {
		this.ID = ID;
		this.isCentroid = isCentroid;
	}
	
	public boolean equals(Node n) {
		return n.getID() == this.ID;
	}

	public Integer getID() {
		return ID;
	}

	public int hashCode() {
		return getID();
	}


	public boolean isCentroid() {
		return isCentroid;
	}


	public String toString() {
		return "Node " + this.ID.toString();
	}
}
