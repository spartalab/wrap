package edu.utexas.wrap;

import java.util.Map;

public abstract class BushBasedOptimizer extends Optimizer {

	public BushBasedOptimizer(Network network) {
		super(network);
	}

	public void optimize() {
		// A single general step iteration
		for (Origin o : network.getOrigins()) {
			
			Boolean altered;
			do{
				Bush b = o.getBush();
				// Step i: Build min- and max-path trees
				b.runDijkstras(false);
				b.runDijkstras(true);

				// Step iia: Equilibrate bush
				equilibrateBush(b);

				// Step iib: Recalculate U labels
				b.runDijkstras(true);
				
				// TODO Step iii: Improve bush
				altered = improveBush(b);

				// Step iv: Reiterate if bush changed
			} while (altered);
		}
	}

	protected abstract void equilibrateBush(Bush b);
	
	protected Boolean improveBush(Bush b) {
		// TODO Auto-generated method stub
		Boolean modified = false;
		Map<Link, Boolean> links = b.getLinks();
		for (Link l : links.keySet()) {
			// If link is active, do nothing (removing flow should mark as inactive)
			if (links.get(l)) continue;
			// Else if Ui + tij < Uj
			else if (b.getU(l.getTail())
					+ l.getTravelTime() 
					< b.getU(l.getHead())) {
				links.put(l, true);
				modified = true;
			}
		}
		
		return modified;
	}
	
}