package edu.utexas.wrap;

public class Origin extends Node{
	private Bush bush;
	private int[] demandVector;
	
	public Origin(int id, int latitude, int longitude, int[] demandVector) {
		super(id, latitude, longitude);
		this.demandVector = demandVector;
	}

	public Bush getBush() {
		return bush;
	}

	public void setBush(Bush bush) {
		this.bush = bush;
	}

	public int[] getdemandVector() {
		return demandVector;
	}

	public void setdVector(int[] dVector) {
		this.demandVector = dVector;
	}
	
}
