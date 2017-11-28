package edu.utexas.wrap;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

public class AlgorithmBOptimizer extends Optimizer{

	public AlgorithmBOptimizer(Network network) {
		super(network);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void optimize() {
		// TODO Auto-generated method stub
		
		for (Origin o : getNetwork().getOrigins()) {
			Bush b = o.getBush();
			Iterator<Node> topOrder = b.getTopologicalOrder().descendingIterator();
			
			
			while (topOrder.hasNext()) {
				Node cur = topOrder.next();
				Link shortLink = b.getqShort(cur);
				Link longLink = b.getqLong(cur);
				Set<Node> shortNodes = new HashSet<Node>();
				
				// If there is no divergence node, move on to the next topological node
				if (longLink.equals(shortLink)) {
					continue;
				}
				//Else calculate divergence node

				LinkedList<Link> uPath = new LinkedList<Link>();
				LinkedList<Link> lPath = new LinkedList<Link>();
				Node m,n;
				Float maxDelta = Float.MAX_VALUE;
				Float x;

				//Dump all nodes on short path into a temporary set
				//Note that this isn't optimal but it works for our purposes
				//since we don't use a true linked list for our shortest paths
				//
				//This may be changed in future development
				do {
					n = shortLink.getTail();
					shortNodes.add(n);
					shortLink = b.getqShort(n);
				} while (shortLink != null);

				//Iterate through longest paths until reaching a node in shortest path
				do {
					//Determine the max amount of flow that can be shifted
					x = b.getBushFlow(longLink);
					if (x < maxDelta) {
						maxDelta = x;
					}
					
					m = longLink.getTail();
					//Construct the longest path for PAS
					uPath.addFirst(longLink);
					longLink = b.getqLong(m);
				} while (!shortNodes.contains(m));

				//Reiterate through shortest path to build path up to divergence node
				shortLink = b.getqShort(cur);
				do {
					n = shortLink.getTail();
					//Construct the shortest path for PAS
					lPath.addFirst(shortLink);
					shortLink = b.getqShort(n);
				} while (!m.equals(n));

				//The two paths constitute a Pair of Alternate Segments

				//TODO calculate delta h, capping at maxDelta
				

				//TODO add delta h to all x values in pi_L

				//TODO subtract delta h from all x values in pi_U
			}
		}
	}
}