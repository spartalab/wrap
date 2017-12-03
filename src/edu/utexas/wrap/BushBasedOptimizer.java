package edu.utexas.wrap;

import java.util.Map;

import edu.utexas.wrap.Bush.DijkCases;

public abstract class BushBasedOptimizer extends Optimizer {

	public BushBasedOptimizer(Network network) {
		super(network);
	}

	public void optimize() throws Exception {
		// A single general step iteration
		for (Origin o : network.getOrigins()) {
			
			Boolean altered;
			do{
				Bush b = o.getBush();
				// Step i: Build min- and max-path trees
				b.runDijkstras(Bush.DijkCases.EQUILIBRATE_SHORTEST);
				b.runDijkstras(Bush.DijkCases.LONGEST);

				// Step iia: Equilibrate bush
				equilibrateBush(b);

				// Step iib: Recalculate U labels
				b.runDijkstras(Bush.DijkCases.LONGEST);
				
				// Step iii: Improve bush
				altered = improveBush(b);

				// Step iv: Reiterate if bush changed
			} while (altered);
		}
	}

	protected abstract void equilibrateBush(Bush b) throws Exception;
	
	protected Boolean improveBush(Bush b) {
		Boolean modified = false;
		Map<Link, Boolean> links = b.getLinks();
		for (Link l : links.keySet()) {
			// If link is active, do nothing (removing flow should mark as inactive)
			if (l.getTail().getID().equals(17) && l.getHead().getID().equals(16)) {
				int z = 0;
				z++;
			}
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
	
	public String getResults() {
		//TODO: Improve this method
		for (Origin o : network.getOrigins()) {
			o.getBush().runDijkstras(Bush.DijkCases.EQUILIBRATE_SHORTEST);
		}
		return network.AEC().toString();
		
	}
	
}