package edu.utexas.wrap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.utexas.wrap.Bush.DijkCases;

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
				LinkedList<Node> to = b.getTopologicalOrder();
				//b.runDijkstras(Bush.DijkCases.EQUILIBRATE_SHORTEST);
				b.topoSearch(Bush.DijkCases.EQUILIBRATE_SHORTEST, to);
				b.topoSearch(Bush.DijkCases.LONGEST, to);
				//b.getLongestPaths(to);

				// Step iia: Equilibrate bush
				equilibrateBush(b, to);
				
				//to = b.getTopologicalOrder();
				// Step iib: Recalculate U labels
				//b.runDijkstras(Bush.DijkCases.EQUILIBRATE_SHORTEST);
//				b.topoSearch(Bush.DijkCases.EQUILIBRATE_SHORTEST, to);
//				b.topoSearch(Bush.DijkCases.LONGEST, to);
				//b.getLongestPaths(to);
				
				// Step iii: Improve bush
				altered = improveBush(b);

				// Step iv: Reiterate if bush changed
			} while (altered);
		}
	}

	protected abstract void equilibrateBush(Bush b, LinkedList<Node> to) throws Exception;
	
	protected Boolean improveBush(Bush b) throws Exception {
		boolean modified = false;
		Map<Link, Boolean> links = b.getLinks();
		this.removedLinks = new ArrayList<>();
		int counter = 0;
		
		for (Link l : new HashSet<Link>(b.getLinks().keySet())){
			if(b.getLinks().get(l)){
				if(b.getBushFlow(l)<=0){
					// Check to see if this link is needed for connectivity
					Boolean needed = true;
					for (Link i : l.getHead().getIncomingLinks()) {
						if (!i.equals(l) && b.getLinks().get(i) && !removedLinks.contains(i)) {
							needed = false;
							break;
						}
					}
					if (!needed) {
						b.getLinks().put(l, false);	// deactivate link in bush if no flow left
						removedLinks.add(l);
//						counter++;
					}
				}
			}
		}
		
		LinkedList<Node> to = new LinkedList<>();
		to = b.getTopologicalOrder();
		b.topoSearch(Bush.DijkCases.EQUILIBRATE_SHORTEST, to);
		b.topoSearch(Bush.DijkCases.LONGEST, to);
		
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
			if (b.getU(l.getTail()) + l.getTravelTime() < b.getU(l.getHead())) {
				links.put(l, true);
				if(!this.removedLinks.contains(l)) modified = true;
//				else counter++;
			}
		}
		}
		
		return modified;
	}

	public List<Float> getResults() throws Exception {
		//TODO: Improve this method
	    List<Float> results = new ArrayList<>();
	    for(Origin o : network.getOrigins()) {
	    		LinkedList<Node> to = o.getBush().getTopologicalOrder();
	    		o.getBush().topoSearch(DijkCases.EQUILIBRATE_SHORTEST, to);
	    }

	    results.add(network.AEC());
	    results.add(network.tstt());
		return results;
		
	}
	
}