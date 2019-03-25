package edu.utexas.wrap.assignment.bush;

import java.util.HashSet;

import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.Node;

public class BushMerge extends HashSet<Link> implements BackVector{
	private Link shortLink;
	private Link longLink;
	private Node diverge;
	
	public Link getShortLink() {
		return shortLink;
	}
	
	public Link getLongLink() {
		return longLink;
	}
	
	public Node getDiverge() {
		return diverge;
	}
	
	protected void setShortLink(Link l) {
		shortLink = l;
	}
	
	protected void setLongLink(Link l) {
		longLink = l;
	}
	
	protected void setDiverge(Node n) {
		diverge = n;
	}
	
	public String toString() {
		return "Merge from diverge "+diverge.toString();
	}
	
	//TODO handle adding links
}
