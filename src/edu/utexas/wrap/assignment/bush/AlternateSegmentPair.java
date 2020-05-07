package edu.utexas.wrap.assignment.bush;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.Node;

/**A pair of alternative paths for equilibration, consisting of two paths in
 * a bush, with at least one of the two being utilized already. Flow can be
 * shifted from one path to the other, allowing the assignment to be drawn
 * closer into equilibrium. 
 * @author William
 *
 */
public class AlternateSegmentPair {
	private Node merge, diverge;
	private Double maxDelta;
	private final PathCostCalculator pcc;
	private int longPathLength, shortPathLength;

	public AlternateSegmentPair(Node merge, Node diverge, Double maxDelta, PathCostCalculator pcc, int lpl, int spl) {
		this.diverge = diverge;
		this.merge = merge;
		this.pcc = pcc;
		this.maxDelta = maxDelta;
		longPathLength = lpl;
		shortPathLength = spl;
	}
	
	/**
	 * @return the point from which bush flows diverge
	 */
	public Node diverge() {
		return diverge;
	}
	
	/**
	 * @return the amount of flow which can be shifted from one path to the other
	 */
	public Double maxDelta() {
		return maxDelta;
	}
	
	/**
	 * @return the point at which bush flows merge back
	 */
	public Node merge() {
		return merge;
	}
	
	/**
	 * @return the difference in path costs between the alternate segments
	 */
	public Double priceDiff() {
		Float vot = pcc.getBush().valueOfTime();
		Mode klass = pcc.getBush().vehicleClass();
		
		//For each link in the longest and shortest paths, sum the link prices in parallel through a Stream
		Double longPrice = StreamSupport.stream(longPath().spliterator(), false).unordered().mapToDouble(x -> x.getPrice(vot, klass)).sum();
		Double shortPrice = StreamSupport.stream(shortPath().spliterator(), false).unordered().mapToDouble(x -> x.getPrice(vot, klass)).sum();

		//Perform a quick numerical check
		Double ulp = Math.max(Math.ulp(longPrice),Math.ulp(shortPrice));
		if (longPrice < shortPrice) {
			if (longPrice-shortPrice < 2*ulp) return 0.0;
			else throw new RuntimeException("Longest path cheaper than shortest path");
		}
		
		return longPrice-shortPrice;
	}
	
	/**
	 * @return the bush containing the ASP
	 */
	public Bush getBush() {
		return pcc.getBush();
	}

	/**
	 * @return an iterator over the links in the shortest path of this ASP
	 */
	public Iterable<Link> shortPath() {
		return new ShortPathIterator();
	}

	/**
	 * @return an iterator over the links in the longest path of this ASP
	 */
	public Iterable<Link> longPath() {
		return new LongPathIterator();
	}
	
	/**An iterator that follows the shortest path backvector links until
	 * the diverge node is reached
	 * @author William
	 *
	 */
	private class ShortPathIterator implements Iterable<Link> {
		@Override
		public Iterator<Link> iterator() {
			return new Iterator<Link>(){
				
				Node current = merge;
				
				@Override
				public boolean hasNext() {
					//Stop when the diverge is reached or we run out of path
					return current != diverge && pcc.getqShort(current) != null;
				}

				@Override
				public Link next() {
					//advance the current node back up the bush
					Link qs = pcc.getqShort(current);
					current = qs.getTail();
					return qs;
				}
				
			};
		}
		
		public Spliterator<Link> spliterator(){
			return new ShortPathSpliterator(merge, shortPathLength);
		}
	}
	
	/**An iterator that follows the longest path backvector links until
	 * the diverge node is reached
	 * @author William
	 *
	 */
	private class LongPathIterator implements Iterable<Link>{

		@Override
		public Iterator<Link> iterator() {
			return new Iterator<Link>() {
				
				Node current = merge;
				
				@Override
				public boolean hasNext() {
					//Stop when the diverge is reached or we run out of path
					return current != diverge && pcc.getqLong(current) != null;
				}

				@Override
				public Link next() {
					//advance the current node back up the bush
					Link ql = pcc.getqLong(current);
					current = ql.getTail();
					return ql;
				}
				
			};
		}
		
		public Spliterator<Link> spliterator(){
			return new LongPathSpliterator(merge, longPathLength);			
		}
	}
	
	/**Spliterator that encapsulates all Links in the longest path
	 * @author William
	 *
	 */
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
			return	Spliterator.ORDERED | 	//The reverse ordering of the backvector links
					Spliterator.DISTINCT | 	//The path is acyclic
					Spliterator.SIZED | 	//The exact size of the spliterator is predefined
					Spliterator.NONNULL | 	//No null links should ever be returned
					Spliterator.IMMUTABLE |	//The bush structure shouldn't change while examining ASPs
					Spliterator.SUBSIZED;	//The subdivisions of this Spliterator also have known sizes
		}

		@Override
		public boolean tryAdvance(Consumer<? super Link> action) {
			if (elapsed == size) return false; //We've reached the diverge node
			Link v = pcc.getqLong(cur);	//Advance to the next link
			if (v == null) return false;	//We've reached the end of the road
			
			cur = v.getTail();	//Advance the current node
			elapsed++;
			
			action.accept(v);	//Accept the action on the prior link
			return true;
		}

		@Override
		public Spliterator<Link> trySplit() {
			int curSize = size-elapsed;	//The amount of unvisited links remaining
			if (curSize < 6) return null;	//Optimization TODO revisit this size
			
			int splitPos = curSize / 2;	//Divide in two
			Spliterator<Link> s = new LongPathSpliterator(cur,splitPos);
			
			//Advance backward halfway, skipping the Links allocated to the new Spliterator
			for (int i = 0; i < splitPos; i++) {
				Link v = pcc.getqLong(cur);
				if (v == null) throw new RuntimeException("Wrong longest-path length");
				cur = v.getTail();
				elapsed++;
			}
			
			return s;
		}
	}
	
	
	/**Spliterator that encapsulates all Links in the shortest path
	 * @author William
	 *
	 */
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
			return	Spliterator.ORDERED | 	//The reverse ordering of the backvector links
					Spliterator.DISTINCT | 	//The path is acyclic
					Spliterator.SIZED | 	//The exact size of the spliterator is predefined
					Spliterator.NONNULL | 	//No null links should ever be returned
					Spliterator.IMMUTABLE |	//The bush structure shouldn't change while examining ASPs
					Spliterator.SUBSIZED;	//The subdivisions of this Spliterator also have known sizes
		}
		
		@Override
		public boolean tryAdvance(Consumer<? super Link> action) {
			if (elapsed == size) return false;	//we've reached the diverge node
			Link v = pcc.getqShort(cur); //get the next link
			if (v == null) return false;	//we've run out of path
			
			//advance the current node
			cur = v.getTail();
			elapsed++;
			
			action.accept(v);	//accept the action on the prior link
			return true;
		}
		
		@Override
		public Spliterator<Link> trySplit(){
			int curSize = size - elapsed;	//the remaining number of links
			if (curSize < 6) return null;	//optimization TODO Reevaluate this size
			
			int splitSize = curSize / 2;	//divide the remaining links in half
			Spliterator<Link> s = new ShortPathSpliterator(cur, splitSize);
			
			//advance backward halfway, skipping the links allotted to the new Spliterator
			for (int i = 0; i < splitSize; i++) {
				Link v = pcc.getqShort(cur);
				if (v == null) throw new RuntimeException("Wrong shortest-path length");
				cur = v.getTail();
				elapsed++;
			}
			
			return s;
		}
	}
}
