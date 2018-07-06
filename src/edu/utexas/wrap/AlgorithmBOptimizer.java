package edu.utexas.wrap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;



public class AlgorithmBOptimizer extends BushBasedOptimizer{

	public AlgorithmBOptimizer(Network network) {
		super(network);
	}

	/** Implement the Algorithm B version of bush equilibration
	 * @param b a bush to be equilibrated
	 * @param to the bush's topological order
	 * @throws Exception if there was negative bush flow
	 */
	protected synchronized void equilibrateBush(Bush b) throws Exception {
		
		LinkedList<Node> to = b.getTopologicalOrder();
		Integer index = to.size() - 1;
		Node cur;
		HashMap<Link, Double> deltaX = new HashMap<Link, Double>();
		b.topoSearch(false);
		b.topoSearch(true);
		
		// The LinkedList descendingIterator method wasn't working
//		while (index >= 0) {
//			cur = to.get(index);
//			index --;
		Iterator<Node> it = to.descendingIterator();
		while (it.hasNext()) {
			wrap.dBlock.acquire();
			cur = it.next();
			if (cur.equals(b.getOrigin())) {
				wrap.dBlock.release();
				continue;
			}

			Link shortLink = b.getqShort(cur);
			Link longLink = b.getqLong(cur);
			Set<Node> shortNodes = new HashSet<Node>();
			
			// If there is no divergence node, move on to the next topological node
			if (longLink.equals(shortLink)) {
				wrap.dBlock.release();
				continue;
			}
			//Else calculate divergence node

			Path uPath = new Path();
			Path lPath = new Path();
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
				x = longLink.getBushFlow(b) + deltaX.getOrDefault(longLink, 0.0);
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
			Double denom = 0.0;
			for (Link l : lPath) {
				denom += l.pricePrime(b.getVOT());
			}
			for (Link l : uPath) {
				denom += l.pricePrime(b.getVOT());
			}
//			b.topoSearch(false);
//			b.topoSearch(true);
//			
			
			Double diffU = (b.getU(cur)-b.getU(m));
			Double diffL = (b.getL(cur)-b.getL(m));
			Double deltaH = Double.min(maxDelta,
					( diffU - diffL ) / denom );
			if(!( deltaH >= 0.0)) throw new RuntimeException();
			//add delta h to all x values in pi_L
			for (Link l : lPath) {
//				b.addFlow(l, deltaH);
				Double t = deltaX.getOrDefault(l, 0.0) + deltaH;
				
				deltaX.put(l, t);
			}
			
			//subtract delta h from all x values in pi_U
			for (Link l : uPath) {
//				b.subtractFlow(l, deltaH);
				Double t = deltaX.getOrDefault(l, 0.0) - deltaH;
				if (t < -l.getBushFlow(b)) throw new NegativeFlowException("too much bush flow removed");
				if (t < -l.getFlow() ) {
					throw new NegativeFlowException("too much link flow removed");
				}
				deltaX.put(l,t);
			}
			
			wrap.dBlock.release();
		}
		for (Link z : new HashSet<Link>(deltaX.keySet())) {
			wrap.dBlock.acquire();
			Double t = deltaX.get(z);
			synchronized(z) {

				
			if(!( z.getBushFlow(b) + t >= 0 && z.getFlow() + t >= 0)) throw new NegativeFlowException("");
			b.changeFlow(z, t);
			}
			wrap.dBlock.release();
		}
		
	}

	@Override
	public String toString() {
		return "Algorithm B Optimizer";
	}

}
