package edu.utexas.wrap;

public class Link {
	private float bprValue;
	private int capacity;
	private int currentVolume;
	private float ffSpeed;
	private Node head;
	private Node tail;
	
	
	
	public Link(float bprValue, int capacity, int currentVolume, float ffSpeed, Node head, Node tail) {
		super();
		this.setBprValue(bprValue);
		this.capacity = capacity;
		this.currentVolume = currentVolume;
		this.ffSpeed = ffSpeed;
		this.head = head;
		this.tail = tail;
	}
	
	
	public Link(Node tail, Node head, Integer capacity, Integer length, Double fftime, Double b, Integer power) {
		// TODO Auto-generated constructor stub
	}


	public float getBprValue() {
		return bprValue;
	}
	public void setBprValue(float bprValue) {
		this.bprValue = bprValue;
	}
	public int getCapacity() {
		return capacity;
	}
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}
	public int getCurrentVolume() {
		return currentVolume;
	}
	public void setCurrentVolume(int currentVolume) {
		this.currentVolume = currentVolume;
	}
	public float getFfSpeed() {
		return ffSpeed;
	}
	public void setFfSpeed(float ffSpeed) {
		this.ffSpeed = ffSpeed;
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


	public Double getTravelTime() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
