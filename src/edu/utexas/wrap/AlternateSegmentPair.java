package edu.utexas.wrap;

import java.util.Map;

public class AlternateSegmentPair {
	private final Path longPath;
	private final Path shortPath;
	private final Bush bush;

	public AlternateSegmentPair(Path shortPath, Path longPath, Bush bush) {
		this.longPath = longPath;
		this.shortPath = shortPath;
		this.bush = bush;
	}
	
	public Double getMaxDelta(Map<Link, Double> deltaX) {
		return longPath.getMinFlow(bush, deltaX);
	}
	
	public Node getDiverge() {
		return shortPath.node(0);
	}
	
	public Node getTerminus() {
		return shortPath.getLast().getHead();
	}
	
	public Path getShortPath() {
		return shortPath;
	}
	
	public Path getLongPath() {
		return longPath;
	}
	
	public Double getPriceDiff() {
		Double longPrice = longPath.getPrice(bush.getVOT(), bush.getVehicleClass());				
		Double shortPrice = shortPath.getPrice(bush.getVOT(), bush.getVehicleClass()); 
		Double ulp = Math.max(Math.ulp(longPrice),Math.ulp(shortPrice));
		if (longPrice< shortPrice) {
			if (longPrice-shortPrice < 2*ulp) return 0.0;
			else throw new RuntimeException("Longest path cheaper than shortest path");
		}
		
		return longPrice-shortPrice;
	}
}
