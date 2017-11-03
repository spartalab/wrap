package edu.utexas.wrap;

import java.util.List;

public class Node {

	private List<Link> incomingLinks;
	private List<Link> outgoingLinks;
	private Integer ID;
	
	public Node(List<Link> incomingLinks, List<Link> outgoingLinks, Integer ID) {
		this.incomingLinks = incomingLinks;
		this.outgoingLinks = outgoingLinks;
		this.ID = ID;
	}

	
	public Node(Integer ID) {
		this.ID = ID;
	}

	public List<Link> getIncomingLinks() {
		return incomingLinks;
	}

	public void setIncomingLinks(List<Link> incomingLinks) {
		this.incomingLinks = incomingLinks;
	}

	public List<Link> getOutgoingLinks() {
		return outgoingLinks;
	}

	public void setOutgoingLinks(List<Link> outgoingLinks) {
		this.outgoingLinks = outgoingLinks;
	}
	
	public boolean equals(Node n) {
		return n.getID() == this.ID;
	}

	public Integer getID() {
		return ID;
	}
	
}
