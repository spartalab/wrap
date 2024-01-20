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
	
	public String toString() {
		return "Movement "+tailLink.getTail().getID().toString()+"->"
				+tailLink.getHead().getID().toString()+"->"
				+headLink.getHead().getID().toString();
	}
}
