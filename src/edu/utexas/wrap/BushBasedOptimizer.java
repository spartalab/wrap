package edu.utexas.wrap;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class BushBasedOptimizer extends Optimizer {
	private int innerIters;

	public BushBasedOptimizer(Network network) {
		super(network);
		innerIters = 10;
	}

	public BushBasedOptimizer(Network network, Integer maxIters) {
		super(network, maxIters, -6);
		innerIters = 10;
	}
	
	public BushBasedOptimizer(Network network, Integer maxIters, Integer exp) {
		super(network, maxIters, exp, 16);
		innerIters = 10;
	}

	public BushBasedOptimizer(Network network, Integer maxIters, Integer exp, Integer places) {
		super(network,maxIters,exp,places);
		innerIters = 10;
	}
	
	public BushBasedOptimizer(Network network, Integer maxIters, Integer exp, Integer places, Integer innerIters) {
		super(network,maxIters,exp,places);
		this.innerIters = innerIters;
	}
	
	protected abstract void equilibrateBush(Bush b);

	protected Boolean improveBush(Bush b) {
		//TODO cleanup

		b.prune();

		boolean modified = false;
		Set<Link> usedLinks = new HashSet<Link>(b);
		Set<Link> unusedLinks = new HashSet<Link>(network.getLinks());
		unusedLinks.removeAll(usedLinks);
		
		b.shortTopoSearch();
		Map<Node, Double> cache = b.longTopoSearch();
		
		for (Link l : unusedLinks) {
			// If link is active, do nothing (removing flow should mark as inactive)
			//Could potentially delete both incoming links to a node
			if (!l.allowsClass(b.getVehicleClass()) || b.isInvalidConnector(l)) continue;
			try {
				// Else if Ui + tij < Uj
				
				Double tailU = b.getCachedU(l.getTail(), cache);
				Double headU = b.getCachedU(l.getHead(), cache);
			
				
				if (tailU + (l.getPrice(b.getVOT(),b.getVehicleClass())) < headU) {
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
		try {
			for (Thread t : pool) {
				t.join();

			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < innerIters; i++) {
			for (Origin o : network.getOrigins()) {
				for (Bush b : o.getBushes()) {
					// Step i: Equilibrate bushes sequentially
					equilibrateBush(b);
				}
			}
		}
	}
}
