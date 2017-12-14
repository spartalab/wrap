package edu.utexas.wrap;

import java.util.ArrayList;
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

			Bush b = o.getBush();

			// Step i: Build min- and max-path trees
			LinkedList<Node> to = b.getTopologicalOrder();
			b.topoSearch(false, to);
			b.topoSearch(true, to);

			// Step iia: Equilibrate bush
			equilibrateBush(b, to);
		}

		// Step iib: Recalculate U labels
		for (Origin o : network.getOrigins()) {
			Bush b = o.getBush();

			// Step iii: Improve bush
			improveBush(b);
		}
		// Step iv: Reiterate if bush changed

	}

	protected abstract void equilibrateBush(Bush b, LinkedList<Node> to) throws Exception;
	
	protected Boolean improveBush(Bush b) throws Exception {
		boolean modified = false;
		Map<Link, Boolean> links = b.getLinks();
		this.removedLinks = new ArrayList<>();
		
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
					}
				}
			}
		}
		
		LinkedList<Node> to = new LinkedList<>();
		to = b.getTopologicalOrder();
		b.topoSearch(false, to);
		b.topoSearch(true, to);
		
		for (Link l : new HashSet<Link>(links.keySet())) {
			// If link is active, do nothing (removing flow should mark as inactive)
			//Could potentially delete both incoming links to a node
			if (!links.get(l)) {

				// Else if Ui + tij < Uj
				if (b.getU(l.getTail()) + l.getTravelTime() < b.getU(l.getHead())) {
					links.put(l, true);
					if(!this.removedLinks.contains(l)) modified = true;
				}
			}
		}
		return modified;
	}

	public List<Double> getResults() throws Exception {
		//TODO: Improve this method
	    List<Double> results = new ArrayList<>();
	    for(Origin o : network.getOrigins()) {
	    		LinkedList<Node> to = o.getBush().getTopologicalOrder();
	    		o.getBush().topoSearch(false, to);
	    }

	    results.add(network.AEC());
	    results.add(network.tstt());
	    results.add(network.Beckmann());
		return results;
		
	}
	
}