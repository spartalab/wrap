package edu.utexas.wrap;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class BushBasedOptimizer extends Optimizer {


	public BushBasedOptimizer(Network network) {
		super(network);
	}

	public synchronized void iterate() {
		// A single general step iteration
		// TODO explore which bushes should be examined 
		for (Origin o : network.getOrigins()) {
			for (Bush b : o.getBushes()) {
				// Step i: Equilibrate bush
				equilibrateBush(b);
				// Step ii: Improve bush
				improveBush(b);
				
			}
		}
	}

	protected abstract void equilibrateBush(Bush b);

	protected Boolean improveBush(Bush b) {
		//TODO cleanup


		b.prune();

		boolean modified = false;
		Set<Link> usedLinks = new HashSet<Link>(b.getLinks());
		Set<Link> unusedLinks = new HashSet<Link>(network.getLinks());
		unusedLinks.removeAll(usedLinks);
		
		b.topoSearch(false);
		b.topoSearch(true);
		
		Map<Node, BigDecimal> cache = new HashMap<Node, BigDecimal>(network.numNodes());
		for (Link l : new HashSet<Link>(unusedLinks)) {
			// If link is active, do nothing (removing flow should mark as inactive)
			//Could potentially delete both incoming links to a node
			try {
				// Else if Ui + tij < Uj
				
				BigDecimal tailU = b.getCachedU(l.getTail(), cache);
				BigDecimal headU = b.getCachedU(l.getHead(), cache);
			
				
				if (tailU.add(l.getPrice(b.getVOT())).compareTo(headU)<0) {
					b.activate(l);
					modified = true;
				}
			} catch (UnreachableException e) {
				if (e.demand > 0) e.printStackTrace();
				continue;
			}

		}
		return modified;
	}
}
