package edu.utexas.wrap;

import java.util.HashSet;
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
		incomingLinks = new HashSet<Link>();
		outgoingLinks = new HashSet<Link>();
	}

	public Set<Link> getIncomingLinks() {
		return incomingLinks;
	}

	protected void setIncomingLinks(Set<Link> incomingLinks) {
		this.incomingLinks = incomingLinks;
	}

	public Set<Link> getOutgoingLinks() {
		return outgoingLinks;
	}

	protected void setOutgoingLinks(Set<Link> outgoingLinks) {
		this.outgoingLinks = outgoingLinks;
	}
	
	public boolean equals(Node n) {
		return n.getID() == this.ID;
	}

	public Integer getID() {
		return ID;
	}


	public void addIncoming(Link link) {
		incomingLinks.add(link);
	}


	public void addOutgoing(Link link) {
		outgoingLinks.add(link);
	}
	
	public String toString() {
		return "Node " + this.ID.toString();
	}
}
