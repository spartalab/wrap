package edu.utexas.wrap;

import java.util.List;

public class Node {

	private int id;
	private double latitude;
	private double longitude;
	private List<Link> incomingLinks;
	private List<Link> outgoingLinks;
	
	public Node(int id, double latitude, double longitude) {
		super();
		this.id = id;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
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
