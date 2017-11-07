package edu.utexas.wrap;

import java.util.Set;

public class Node {

	private Set<Link> incomingLinks;
	private Set<Link> outgoingLinks;
	private final Integer ID;
	
	public Node(Set<Link> incomingLinks, Set<Link> outgoingLinks, Integer ID) {
		this.incomingLinks = incomingLinks;
		this.outgoingLinks = outgoingLinks;
		this.ID = ID;
	}

	
	public Node(Integer ID) {
		this.ID = ID;
	}

	public Set<Link> getIncomingLinks() {
		return incomingLinks;
	}

	public void setIncomingLinks(Set<Link> incomingLinks) {
		this.incomingLinks = incomingLinks;
	}

	public Set<Link> getOutgoingLinks() {
		return outgoingLinks;
	}

	public void setOutgoingLinks(Set<Link> outgoingLinks) {
		this.outgoingLinks = outgoingLinks;
	}
	
	public boolean equals(Node n) {
		return n.getID() == this.ID;
	}

	public Integer getID() {
		return ID;
	}
	
}
