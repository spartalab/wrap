package edu.utexas.wrap.net;

public class TurningMovement {
	private final Link tailLink, headLink;
	private final int id;
	
	public TurningMovement(
			Link tailLink,
			Link headLink,
			int id
			) {
		this.tailLink = tailLink;
		this.headLink = headLink;
		this.id = id;
	}
	
	public Link getHead() {
		return headLink;
	}
	
	public Link getTail() {
		return tailLink;
	}
	
	public int getID() {
		return id;
	}
}
