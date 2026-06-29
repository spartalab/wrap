package edu.utexas.wrap.net;

/**
 * Represents a turning movement at an intersection, defined by the
 * incoming (tail) link and outgoing (head) link. Turning movements
 * are used in signalized intersection modeling to track which signal
 * phases serve which vehicle movements, and to compute movement-specific
 * delays and green time allocations.
 *
 * @author Will Alexander
 * @see SignalGroup
 * @see Ring
 * @see LinkedTurningMovement
 */
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
