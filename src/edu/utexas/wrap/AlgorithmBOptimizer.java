package edu.utexas.wrap;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
	protected synchronized void equilibrateBush(Bush b) {

		LinkedList<Node> to = b.getTopologicalOrder();
		Node cur;
		HashMap<Link, BigDecimal> deltaX = new HashMap<Link, BigDecimal>();
		b.topoSearch(false);
		b.topoSearch(true);

		Iterator<Node> it = to.descendingIterator();
		while (it.hasNext()) {
			cur = it.next();
			if (cur.equals(b.getOrigin())) continue;


			Link shortLink = b.getqShort(cur);
			Link longLink = b.getqLong(cur);
			Set<Node> shortNodes = new HashSet<Node>();

			// If there is no divergence node, move on to the next topological node
			if (longLink.equals(shortLink)) continue;

			//Else calculate divergence node

			Path uPath = new Path();
			Path lPath = new Path();
			Node m,n;
			BigDecimal maxDelta = null;
			BigDecimal x;

			//Dump all nodes on short path into a temporary set
			//Note that this isn't optimal but it works for our purposes
			//since we don't use a true linked list for our shortest paths
			//
			//This may be changed in future development

			// ^^^ what? This needs to be cleaned up for sure TODO
			do {
				n = shortLink.getTail();
				shortNodes.add(n);
				shortLink = b.getqShort(n);
			} while (shortLink != null);

			//Iterate through longest paths until reaching a node in shortest path
			do {
				//Determine the max amount of flow that can be shifted
				x = longLink.getBushFlow(b).add(deltaX.getOrDefault(longLink, BigDecimal.ZERO));
				if (maxDelta == null || x.compareTo(maxDelta) < 0) {
					maxDelta = x;
				}

				m = longLink.getTail();
				//Construct the longest path for PAS
				uPath.addFirst(longLink);
				longLink = b.getqLong(m);
			} while (!shortNodes.contains(m));
			
			if (maxDelta.compareTo(BigDecimal.ZERO) <= 0) continue;
			
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
			BigDecimal denom = BigDecimal.ZERO;
			for (Link l : lPath) denom = denom.add(l.pricePrime(b.getVOT()));			
			for (Link l : uPath) denom = denom.add(l.pricePrime(b.getVOT()));

			try {
				BigDecimal diffU = ((b.getU(cur).subtract(b.getU(m))));
				BigDecimal diffL = ((b.getL(cur)).subtract((b.getL(m))));
				BigDecimal deltaH = maxDelta.min(
						( diffU.subtract(diffL)).divide(denom,RoundingMode.HALF_EVEN));

				if( deltaH.compareTo(BigDecimal.ZERO) < 0) {
					throw new RuntimeException("Longest path shorter than Shortest path");
				}

				//add delta h to all x values in pi_L
				for (Link l : lPath) {
					BigDecimal t = deltaX.getOrDefault(l, BigDecimal.ZERO).add(deltaH);

					deltaX.put(l, t);
				}

				//subtract delta h from all x values in pi_U
				for (Link l : uPath) {
					//				b.subtractFlow(l, deltaH);
					BigDecimal t = deltaX.getOrDefault(l, BigDecimal.ZERO).subtract(deltaH);
					
					if (t.compareTo( l.getBushFlow(b).negate() ) < 0) {
						throw new NegativeFlowException("too much bush flow removed");
					}
					if (t.compareTo( l.getFlow().negate() ) < 0 ) {
						throw new NegativeFlowException("too much link flow removed");
					}
					deltaX.put(l,t);
				}
			} catch (UnreachableException e) {
				throw new RuntimeException("Couldn't calculate deltaH");
			}

		}
		for (Link z : new HashSet<Link>(deltaX.keySet())) {
			BigDecimal t = deltaX.get(z);


			if(!(  t.add(z.getBushFlow(b)).compareTo(BigDecimal.ZERO) >= 0 
					&& t.add(z.getFlow()).compareTo(BigDecimal.ZERO) >= 0)) throw new NegativeFlowException("");
			b.changeFlow(z, t);
		}

	}

	@Override
	public String toString() {
		return "Algorithm B Optimizer";
	}

}
