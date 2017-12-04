package edu.utexas.wrap;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class BushBasedOptimizer extends Optimizer {
	
	protected List<Link> removedLinks;

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
				LinkedList<Node> to = new LinkedList<Node>();
				to = b.getTopologicalOrder();
				b.getLongestPaths(to);

				// Step iia: Equilibrate bush
				equilibrateBush(b);
				
				to = b.getTopologicalOrder();
				// Step iib: Recalculate U labels
				b.runDijkstras(Bush.DijkCases.EQUILIBRATE_SHORTEST);
				b.getLongestPaths(to);
				
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
		for (Link l : new HashSet<Link>(links.keySet())) {
			// If link is active, do nothing (removing flow should mark as inactive)
			//Could potentially delete both incoming links to a node
			if (!links.get(l)) {
//				if (b.getBushFlow(l) <= 0) {
//					// Check to see if this link is needed for connectivity
//					Boolean needed = true;
//					for (Link i : l.getHead().getIncomingLinks()) {
//						if (!i.equals(l) && links.get(i)) {
//							needed = false;
//							break;
//						}
//					}
//					if (!needed) {
//						links.put(l, false);	// deactivate link in bush if no flow left
//						modified = true;
//					}
//				}
//			}

			// Else if Ui + tij < Uj
			if (b.getU(l.getTail())
					+ l.getTravelTime() 
					< b.getU(l.getHead())) {
				links.put(l, true);
				if(!this.removedLinks.contains(l)) modified = true;
			}
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