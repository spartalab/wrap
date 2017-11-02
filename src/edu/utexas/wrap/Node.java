package edu.utexas.wrap;

import java.util.List;

public class Node {

	List<Link> incomingLinks;
	List<Link> outgoingLinks;
	
	public Node(List<Link> incomingLinks, List<Link> outgoingLinks, Integer ID) {
		this.incomingLinks = incomingLinks;
		this.outgoingLinks = outgoingLinks;
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
	
	
	
	
}
