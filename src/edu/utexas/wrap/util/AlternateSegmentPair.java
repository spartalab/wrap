package edu.utexas.wrap.util;

import java.util.Map;

import edu.utexas.wrap.assignment.Path;
import edu.utexas.wrap.assignment.bush.Bush;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.Node;

public class AlternateSegmentPair {
	private final Path longPath;
	private final Path shortPath;
	private final Bush bush;

	public AlternateSegmentPair(Path shortPath, Path longPath, Bush bush) {
		this.longPath = longPath;
		this.shortPath = shortPath;
		this.bush = bush;
	}
	
	public Node diverge() {
		return shortPath.node(0);
	}
	
	public Path longPath() {
		return longPath;
	}
	
	public Double maxDelta(Map<Link, Double> deltaX, Map<Link,Double> flows) {
		return longPath.getMinFlow(bush, deltaX, flows);
	}
	
	public Node merge() {
		return shortPath.getLast().getHead();
	}
	
	public Double priceDiff() {
		Double longPrice = longPath.getPrice(bush.getVOT(), bush.getVehicleClass());				
		Double shortPrice = shortPath.getPrice(bush.getVOT(), bush.getVehicleClass()); 
		Double ulp = Math.max(Math.ulp(longPrice),Math.ulp(shortPrice));
		if (longPrice< shortPrice) {
			if (longPrice-shortPrice < 2*ulp) return 0.0;
			else throw new RuntimeException("Longest path cheaper than shortest path");
		}
		
		return longPrice-shortPrice;
	}
	
	public Path shortPath() {
		return shortPath;
	}
}
