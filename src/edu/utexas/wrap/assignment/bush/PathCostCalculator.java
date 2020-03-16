package edu.utexas.wrap.assignment.bush;

import java.util.Comparator;
import java.util.stream.Collectors;

import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.Node;

public class PathCostCalculator {
	Double[] cache;
	Bush bush;

	public PathCostCalculator(Bush bush) {
		this.bush = bush;
	}
	
	public boolean checkForShortcut(Link l) {
		synchronized (this) {
			if (cache == null) getLongestPathCosts(true);
		}

		double uTail = cache[l.getTail().getOrder()];
		double uHead = cache[l.getHead().getOrder()];
		
		
		return uTail + l.getPrice(bush) < uHead;
	}
	
	public Link getShortestPathLink(BushMerge bm) {
		synchronized (this) {
			if (cache == null) getShortestPathCosts();
		}

		return bm.getLinks().parallel().min(Comparator.comparing( (Link x) -> 
		cache[x.getTail().getOrder()]+x.getPrice(bush)
				)).orElseThrow(RuntimeException::new);
	}

	private void getShortestPathCosts() {
		// TODO Auto-generated method stub
		bush.clear();
		Node[] to = bush.getTopologicalOrder(false);
		cache = new Double[bush.size()];

		//In topological order,
		for (Node d : to) {
			if (d == null) continue;
			else if (d == bush.root().node()) cache[d.getOrder()] = 0.;

			BackVector bv = bush.getBackVector(d);
			if (bv instanceof Link) shortRelax((Link) bv);
			else if (bv instanceof BushMerge) 
				((BushMerge) bv).getLinks().sequential().forEach( l ->
				//Try to relax all incoming links
				shortRelax(l)
						);

		}
	}
	
	/** Relax the shortest path while doing topological shortest path search
	 * @param l the link to examine as  a candidate for shortest path
	 * @param cache a cache of shortest path costs
	 * @throws UnreachableException if a node can't be reached
	 */
	private void shortRelax(Link l) {
		//Calculate the cost of adding this link to the shortest path
		Double Licij = l.getPrice(bush) + cache[l.getTail().getOrder()];
		Node head = l.getHead();
		BackVector back = bush.getBackVector(head);
		
		//If the shortest path doesn't exist, already flows through the link, or this has a lower cost,
		if (cache[head.getOrder()] == null || Licij < cache[head.getOrder()]) {
			//Update the BushMerge, if applicable
			if (back instanceof BushMerge) ((BushMerge) back).setShortLink(l);
			//Store this cost in the cache
			cache[head.getOrder()] = Licij;
		}
	}

	private void getLongestPathCosts(boolean longestUsed) {
		bush.clear();
		Node[] to = bush.getTopologicalOrder(false);
		cache = new Double[bush.size()];

		//In topological order,
		for (Node d : to) {
			if (d == null) continue;
			else if (d == bush.root().node()) cache[d.getOrder()] = 0.;
			
			//Try to relax the backvector (all links in the BushMerge, if applicable)
			BackVector bv = bush.getBackVector(d);
			if (bv instanceof Link) longRelax((Link) bv);
			else if (bv instanceof BushMerge) {
				BushMerge bm = (BushMerge) bv;
				for (Link l : bm.getLinks()
						.filter(l -> !longestUsed || bm.getSplit(l) > 0)
						.collect(Collectors.toSet())) 
					longRelax(l);
			}

		}		
		return;
	}
	
	private void longRelax(Link l) {
		//Calculate the cost of adding the link to the longest path
		Double Uicij = l.getPrice(bush) 
				+ cache[l.getTail().getOrder()];
		Node head = l.getHead();

		BackVector back = bush.getBackVector(head);
		//If the longest path doesn't exist, is already through the proposed link, or the cost is longer,
		if (cache[head.getOrder()] == null || Uicij > cache[head.getOrder()]) {
			//Update the BushMerge if need be
			if (back instanceof BushMerge) ((BushMerge) back).setLongLink(l);
			//Store the cost in the cache
			cache[head.getOrder()] = Uicij;
		}
	}

	
}
