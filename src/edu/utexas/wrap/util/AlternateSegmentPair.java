package edu.utexas.wrap.util;

import java.util.Iterator;

import edu.utexas.wrap.assignment.bush.Bush;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.Node;

public class AlternateSegmentPair {
	private Node merge, diverge;
	private Double maxDelta;
	private final Bush bush;

	public AlternateSegmentPair(Node merge, Node diverge, Double maxDelta, Bush bush) {
		this.diverge = diverge;
		this.merge = merge;
		this.bush = bush;
		this.maxDelta = maxDelta;
	}
	
	public Node diverge() {
		return diverge;
	}
	
	public Double maxDelta() {
		return maxDelta;
	}
	
	public Node merge() {
		return merge;
	}
	
	public Double priceDiff() {
		Double longPrice = 0.0, shortPrice = 0.0;
		Float vot = bush.getVOT();
		Mode klass = bush.getVehicleClass();
		
		for (Link l : longPath()) longPrice += l.getPrice(vot, klass);				
		for (Link l : shortPath()) shortPrice += l.getPrice(vot, klass); 
		
		Double ulp = Math.max(Math.ulp(longPrice),Math.ulp(shortPrice));
		if (longPrice < shortPrice) {
			if (longPrice-shortPrice < 2*ulp) return 0.0;
			else throw new RuntimeException("Longest path cheaper than shortest path");
		}
		
		return longPrice-shortPrice;
	}
	
	public Bush getBush() {
		return bush;
	}

	public Iterable<Link> shortPath() {
		return new ShortPathIterator();
	}

	public Iterable<Link> longPath() {
		return new LongPathIterator();
	}
	
	private class ShortPathIterator implements Iterable<Link> {

		@Override
		public Iterator<Link> iterator() {
			return new Iterator<Link>(){
				Node current = merge;
				@Override
				public boolean hasNext() {
					return current != diverge && bush.getqShort(current) != null;
				}

				@Override
				public Link next() {
					Link qs = bush.getqShort(current);
					current = qs.getTail();
					return qs;
				}
				
			};
		}
		
	}
	private class LongPathIterator implements Iterable<Link>{

		@Override
		public Iterator<Link> iterator() {
			return new Iterator<Link>() {
				Node current = merge;
				@Override
				public boolean hasNext() {
					return current != diverge && bush.getqLong(current) != null;
				}

				@Override
				public Link next() {
					Link ql = bush.getqLong(current);
					current = ql.getTail();
					return ql;
				}
				
			};
		}
		
	}
}
