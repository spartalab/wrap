package edu.utexas.wrap;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class BushBasedOptimizer extends Optimizer {


	public BushBasedOptimizer(Network network) {
		super(network);
	}

	public BushBasedOptimizer(Network network, Integer maxIters) {
		super(network, maxIters, -6);
	}
	
	public BushBasedOptimizer(Network network, Integer maxIters, Integer exp) {
		super(network, maxIters, exp, 16);
	}

	public BushBasedOptimizer(Network network, Integer maxIters, Integer exp, Integer places) {
		super(network,maxIters,exp,places);
	}
	
	public void iterate() {
		// A single general step iteration
		// TODO explore which bushes should be examined 
		
		
		Set<Thread> pool = new HashSet<Thread>();
		
		for (Origin o : network.getOrigins()) {
			for (Bush b : o.getBushes()) {
				
				Thread t = new Thread() {
					public void run() {
						// Step ii: Improve bushes in parallel
						improveBush(b);
					}
				};
				pool.add(t);
				t.start();
			}
		}
		for (Thread t : pool) {
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		
		for (Origin o : network.getOrigins()) {
			for (Bush b : o.getBushes()) {
						// Step i: Equilibrate bushes sequentially
						equilibrateBush(b);

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
		
//		network.acquireLocks();
		b.topoSearch(false);
		Map<Node, BigDecimal> cache = b.topoSearch(true);
//		Map<Node, BigDecimal> cache = new HashMap<Node, BigDecimal>(network.numNodes());
		
		for (Link l : unusedLinks) {
			// If link is active, do nothing (removing flow should mark as inactive)
			//Could potentially delete both incoming links to a node
			if (!l.allowsClass(b.getVehicleClass())) continue;
			try {
				// Else if Ui + tij < Uj
				
				BigDecimal tailU = b.getCachedU(l.getTail(), cache);
				BigDecimal headU = b.getCachedU(l.getHead(), cache);
			
				
				if (tailU.add(l.getPrice(b.getVOT(),b.getVehicleClass())).compareTo(headU)<0) {
					b.activate(l);
					modified = true;
				}
			} catch (UnreachableException e) {
				if (e.demand > 0) e.printStackTrace();
				continue;
			}

		}
//		network.releaseLocks();
		return modified;
	}
}
