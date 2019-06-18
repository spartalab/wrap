package edu.utexas.wrap.util;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

import edu.utexas.wrap.assignment.bush.Bush;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.Node;

public class AlternateSegmentPair {
	private Node merge, diverge;
	private Double maxDelta;
	private final Bush bush;
	private int longPathLength, shortPathLength;

	public AlternateSegmentPair(Node merge, Node diverge, Double maxDelta, Bush bush, int lpl, int spl) {
		this.diverge = diverge;
		this.merge = merge;
		this.bush = bush;
		this.maxDelta = maxDelta;
		longPathLength = lpl;
		shortPathLength = spl;
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
//		Double longPrice = 0.0, shortPrice = 0.0;
		Float vot = bush.getVOT();
		Mode klass = bush.getVehicleClass();
		
		Double longPrice = StreamSupport.stream(longPath().spliterator(), true).unordered().mapToDouble(x -> x.getPrice(vot, klass)).sum();
		Double shortPrice = StreamSupport.stream(shortPath().spliterator(), true).unordered().mapToDouble(x -> x.getPrice(vot, klass)).sum();
//		for (Link l : longPath()) longPrice += l.getPrice(vot, klass);				
//		for (Link l : shortPath()) shortPrice += l.getPrice(vot, klass); 
		
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
		
		public Spliterator<Link> spliterator(){
			return new ShortPathSpliterator(merge, shortPathLength);
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
		
		public Spliterator<Link> spliterator(){
			return new LongPathSpliterator(merge, longPathLength);			
		}
	}
	
	private class LongPathSpliterator implements Spliterator<Link>{
		private int size, elapsed;
		private Node cur;
		
		public LongPathSpliterator(Node start, int size) {
			this.size = size;
			this.cur = start;
			elapsed = 0;
		}
		
		@Override
		public long estimateSize() {
			return size - elapsed;
		}

		@Override
		public int characteristics() {
			// TODO Auto-generated method stub
			return	Spliterator.ORDERED | 
					Spliterator.DISTINCT | 
					Spliterator.SIZED | 
					Spliterator.NONNULL | 
					Spliterator.IMMUTABLE |
					Spliterator.SUBSIZED;
		}

		@Override
		public boolean tryAdvance(Consumer<? super Link> action) {
			if (elapsed == size) return false;
			Link v = bush.getqLong(cur);
			if (v == null) return false;
			cur = v.getTail();
			elapsed++;
			action.accept(v);
			return true;
		}

		@Override
		public Spliterator<Link> trySplit() {
			// TODO Auto-generated method stub
			int curSize = size-elapsed;
			if (curSize < 10) return null;
			int splitPos = curSize / 2;
			Spliterator<Link> s = new LongPathSpliterator(cur,splitPos);
			for (int i = 0; i < splitPos; i++) {
				Link v = bush.getqLong(cur);
				if (v == null) throw new RuntimeException("Wrong longest-path length");
				cur = v.getTail();
				elapsed++;
			}
			return s;
		}
	}
	
	
	private class ShortPathSpliterator implements Spliterator<Link>{
		private int size, elapsed;
		private Node cur;
		
		public ShortPathSpliterator(Node start, int size) {
			this.size = size;
			cur = start;
			elapsed = 0;
		}
		
		@Override
		public long estimateSize() {
			return size - elapsed;
		}
		
		@Override
		public int characteristics() {
			return	Spliterator.ORDERED
					| Spliterator.DISTINCT
					| Spliterator.SIZED
					| Spliterator.NONNULL
					| Spliterator.IMMUTABLE
					| Spliterator.SUBSIZED;
		}
		
		@Override
		public boolean tryAdvance(Consumer<? super Link> action) {
			if (elapsed == size) return false;
			Link v = bush.getqShort(cur);
			if (v == null) return false;
			cur = v.getTail();
			elapsed++;
			action.accept(v);
			return true;
		}
		
		@Override
		public Spliterator<Link> trySplit(){
			int curSize = size - elapsed;
			if (curSize < 10) return null;
			int splitSize = curSize / 2;
			Spliterator<Link> s = new ShortPathSpliterator(cur, splitSize);
			for (int i = 0; i < splitSize; i++) {
				Link v = bush.getqShort(cur);
				if (v == null) throw new RuntimeException("Wrong shortest-path length");
				cur = v.getTail();
				elapsed++;
			}
			return s;
		}
	}
}
