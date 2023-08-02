package edu.utexas.wrap.net;

public class TurningMovement {
	private final Link tailLink, headLink;
	
	public TurningMovement(
			Link tailLink,
			Link headLink
			) {
		this.tailLink = tailLink;
		this.headLink = headLink;
	}
	
	public Link getHead() {
		return headLink;
	}
	
	public Link getTail() {
		return tailLink;
	}
	
}
