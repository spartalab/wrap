package edu.utexas.wrap;

/**
 * @author rahulpatel
 *
 */
public class Link {

	private Double capacity;
	private Node head;
	private Node tail;
	private Double length;
	private double fftime;
	private double b;
	private Double power;
	private double flow;

	
	
	public Link(Node tail, Node head, Double capacity, Double length, Double fftime, Double b, Double power) {
		this.tail = tail;
		this.head = head;
		this.capacity = capacity;
		this.length = length;
		this.fftime = fftime;
		this.b = b;
		this.power = power;
		this.flow = 0.0;
	}

	//B and power are empirical constants in the BPR function
	public double getBValue() {
		return this.b;
	}
	public void setBValue(double bvalue) {
		this.b = bvalue;
	}
	public double getPower() {
		return power;
	}
	public void setPower(Double power) {
		this.power = power;
	}
	public Double getCapacity() {
		return capacity;
	}
	public void setCapacity(Double capacity) {
		this.capacity = capacity;
	}
	public double getFfTime() {
		return fftime;
	}
	public void setFfTime(float fftime) {
		this.fftime = fftime;
	}
	public Node getHead() {
		return head;
	}
	public void setHead(Node head) {
		this.head = head;
	}
	public Node getTail() {
		return tail;
	}
	public void setTail(Node tail) {
		this.tail = tail;
	}
	public Double getLength() {
		return length;
	}
	public void setLength(Double length) {
		this.length = length;
	}
	public double getFlow() {
		return this.flow;
	}
	public void setFlow(double flow) {
		this.flow = flow;
	}
	//Used to add deltaflow to current link flow
	public void addFlow(double deltaflow) {
		this.flow += deltaflow;
	}

	/**BPR Function
	 * A link performance function using empirical constants (b and power) and 
	 * link characteristics (current flow, capacity, & free flow travel time)
	 * @return travel time for the link at current flow
	 */
	public Double getTravelTime() {
		Double tt = getFfTime()*(1.0 + getBValue()*Math.pow(getFlow()/getCapacity(), getPower()));
		return tt;
	}
	
	public String toString() {
		return this.tail.toString() + " -> " + this.head.toString();
	}
	
}
