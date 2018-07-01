package edu.utexas.wrap;

import java.util.HashSet;
import java.util.Set;

public abstract class BushBasedOptimizer extends Optimizer {


	public BushBasedOptimizer(Network network) {
		super(network);
	}

	public void optimize() throws Exception {
		// A single general step iteration
		for (Origin o : network.getOrigins()) {
			for (Bush b : o.getBushes()) {

				// Step i: Build min- and max-path trees
				b.topoSearch(false);
				b.topoSearch(true);

				// Step iia: Equilibrate bush
				equilibrateBush(b);
				improveBush(b);
			}
		}

		// Step iib: Recalculate U labels
//		for (Origin o : network.getOrigins()) {
//			for (Bush b : o.getBushes()) {
//				// Step iii: Improve bush
//				b.topoSearch(false);
//				b.topoSearch(true);
//				improveBush(b);
//			}
//		}
		// Step iv: Reiterate if bush changed

	}

	protected abstract void equilibrateBush(Bush b) throws Exception;

	protected Boolean improveBush(Bush b) throws Exception {
		boolean modified = false;
		Set<Link> usedLinks = new HashSet<Link>(b.getLinks());
		Set<Link> unusedLinks = new HashSet<Link>(network.getLinks());
		unusedLinks.removeAll(usedLinks);
		Set<Link> removedLinks = new HashSet<Link>();

		for (Link l : new HashSet<Link>(usedLinks)){
			if(b.getBushFlow(l)<=0){
				// Check to see if this link is needed for connectivity
				Boolean needed = true;
				for (Link i : l.getHead().getIncomingLinks()) {
					if (!i.equals(l) && usedLinks.contains(i) && !removedLinks.contains(i)) {
						needed = false;
						break;
					}
				}
				if (!needed) {
					usedLinks.remove(l);	// deactivate link in bush if no flow left
					unusedLinks.add(l);
					removedLinks.add(l);
				}
			}

		}
		b.setActive(usedLinks);
		
		b.topoSearch(false);
		b.topoSearch(true);

		for (Link l : new HashSet<Link>(unusedLinks)) {
			// If link is active, do nothing (removing flow should mark as inactive)
			//Could potentially delete both incoming links to a node
			try {
				// Else if Ui + tij < Uj
				if (b.getU(l.getTail()) + l.getPrice(b.getVOT()) < b.getU(l.getHead())) {
					usedLinks.add(l);
					unusedLinks.remove(l);
					if(!removedLinks.contains(l)) modified = true;
				}
			} catch (UnreachableException e) {
				if (e.demand > 0) e.printStackTrace();
				continue;
			}

		}
		b.setActive(usedLinks);
		return modified;
	}
}
