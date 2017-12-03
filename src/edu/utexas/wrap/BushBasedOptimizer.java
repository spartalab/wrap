package edu.utexas.wrap;

import java.util.Map;

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
				if (o.getID().equals(16)) {
					int z = 0;
					z++;
				}
				// Step i: Build min- and max-path trees
				b.runDijkstras(Bush.DijkCases.EQUILIBRATE_SHORTEST);
				b.runDijkstras(Bush.DijkCases.LONGEST);

				// Step iia: Equilibrate bush
				equilibrateBush(b);

				// Step iib: Recalculate U labels
				b.runDijkstras(Bush.DijkCases.EQUILIBRATE_SHORTEST);
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

			if (links.get(l)) {
				if (b.getBushFlow(l) <= 0) {
					// Check to see if this link is needed for connectivity
					Boolean needed = true;
					for (Link i : l.getHead().getIncomingLinks()) {
						if (!i.equals(l) && links.get(i)) {
							needed = false;
							break;
						}
					}
					if (!needed) {
						links.put(l, false);	// deactivate link in bush if no flow left
						modified = true;
					}
				}
			}

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