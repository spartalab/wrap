package edu.utexas.wrap;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;


public class AlgorithmBOptimizer extends BushBasedOptimizer{

	public AlgorithmBOptimizer(Network network) {
		super(network);
	}

	public AlgorithmBOptimizer(Network network, Integer maxIters) {
		super(network, maxIters, -6);
	}

	public AlgorithmBOptimizer(Network network, Integer maxIters, Integer exp) {
		super(network, maxIters, exp, 16);
	}

	public AlgorithmBOptimizer(Network network, Integer maxIters, Integer exp, Integer places) {
		super(network,maxIters,exp,places);
	}

	/** Implement the Algorithm B version of bush equilibration
	 * @param b a bush to be equilibrated
	 * @param to the bush's topological order
	 * @throws Exception if there was negative bush flow
	 */
	protected void equilibrateBush(Bush b) {
		b.acquireLocks();
		LinkedList<Node> to = b.getTopologicalOrder();
		Node cur;
		HashMap<Link, BigDecimal> deltaX = new HashMap<Link, BigDecimal>();
		b.topoSearch(false);
		b.topoSearch(true);

		Iterator<Node> it = to.descendingIterator();
		while (it.hasNext()) {
			cur = it.next();
			if (cur.equals(b.getOrigin())) continue;


			AlternateSegmentPair asp = b.getShortLongASP(cur);
			if (asp == null || asp.getMaxDelta(deltaX).compareTo(BigDecimal.ZERO) <= 0) continue;


			//Iterate through longest paths until reaching a node in shortest path

			//Reiterate through shortest path to build path up to divergence node
			//The two paths constitute a Pair of Alternate Segments

			//calculate delta h, capping at maxDelta
			BigDecimal denom = BigDecimal.ZERO;
			for (Link l : asp.getShortPath()) denom = denom.add(l.pricePrime(b.getVOT()));			
			for (Link l : asp.getLongPath()) denom = denom.add(l.pricePrime(b.getVOT()));

			BigDecimal deltaH = asp.getMaxDelta(deltaX).min(
					asp.getPriceDiff().divide(denom,RoundingMode.HALF_EVEN));

			
			//add delta h to all x values in pi_L
			for (Link l : asp.getShortPath()) {
				BigDecimal t = deltaX.getOrDefault(l, BigDecimal.ZERO).add(deltaH);

				deltaX.put(l, t);
			}

			//subtract delta h from all x values in pi_U
			for (Link l : asp.getLongPath()) {
				//				b.subtractFlow(l, deltaH);
				BigDecimal t = deltaX.getOrDefault(l, BigDecimal.ZERO).subtract(deltaH);

				if (t.compareTo( l.getBushFlow(b).negate() ) < 0) throw new NegativeFlowException("too much bush flow removed");
				if (t.compareTo( l.getFlow().negate() ) < 0 ) throw new NegativeFlowException("too much link flow removed");

				deltaX.put(l,t);
			}
		}

		for (Link z : new HashSet<Link>(deltaX.keySet())) {
			b.changeFlow(z, deltaX.get(z));
		}
		
		b.releaseLocks();
	}

	@Override
	public String toString() {
		return "Algorithm B Optimizer";
	}

}
