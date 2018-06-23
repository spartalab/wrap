package edu.utexas.wrap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class AlgorithmBOptimizer extends BushBasedOptimizer{

	public AlgorithmBOptimizer(Network network) {
		super(network);
	}

	/** Implement the Algorithm B version of bush equilibration
	 * @param b a bush to be equilibrated
	 * @param to the bush's topological order
	 * @throws Exception if there was negative bush flow
	 */
	protected void equilibrateBush(Bush b, LinkedList<Node> to) throws Exception {
		Integer index = to.size() - 1;
		Node cur;
		HashMap<Link, Double> deltaX = new HashMap<Link, Double>();
		for (Link z : b.getLinks().keySet()) deltaX.put(z, new Double(0));
		
		// The LinkedList descendingIterator method wasn't working
		while (index >= 0) {
			cur = to.get(index);
			index --;
			if (cur.equals(b.getOrigin())) continue;
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
			Double maxDelta = Double.MAX_VALUE;
			Double x;

			//Dump all nodes on short path into a temporary set
			//Note that this isn't optimal but it works for our purposes
			//since we don't use a true linked list for our shortest paths
			//
			//This may be changed in future development
			
			// ^^^ what? This needs to be cleaned up for sure
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

			//calculate delta h, capping at maxDelta
			Double denom = new Double(0.0);
			for (Link l : lPath) {
				denom += l.pricePrime(b.getVOT());
			}
			for (Link l : uPath) {
				denom += l.pricePrime(b.getVOT());
			}
			Double diffU = (b.getU(cur)-b.getU(m));
			Double diffL = (b.getL(cur)-b.getL(m));
			Double deltaH = Double.min(maxDelta,
					( diffU 
							- diffL ) / denom );

			//add delta h to all x values in pi_L
			for (Link l : lPath) {
				b.addFlow(l, deltaH);
				l.addFlow(deltaH);
				//deltaX.put(l, deltaX.get(l) + deltaH);
			}
			//subtract delta h from all x values in pi_U
			for (Link l : uPath) {
				b.subtractFlow(l, deltaH);
				l.subtractFlow(deltaH);
				//deltaX.put(l, deltaX.get(l) - deltaH);
			}
		}
//		for (Link z : new HashSet<Link>(deltaX.keySet())) 
//			z.addFlow(deltaX.get(z));
	}

	@Override
	public String toString() {
		return "Algorithm B Optimizer";
	}
}
