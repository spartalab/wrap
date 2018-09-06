package edu.utexas.wrap;

import java.math.BigDecimal;
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
	
	public BigDecimal getMaxDelta(Map<Link, BigDecimal> deltaX) {
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
	
	public BigDecimal getPriceDiff() {
		BigDecimal longPrice = longPath.getPrice(bush.getVOT(), bush.getVehicleClass());				
		BigDecimal shortPrice = shortPath.getPrice(bush.getVOT(), bush.getVehicleClass()); 
		if( longPrice.compareTo(shortPrice) < 0) throw new RuntimeException("Longest path shorter than Shortest path");
		
		return longPrice.subtract(shortPrice);
	}
}
