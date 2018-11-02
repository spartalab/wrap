package edu.utexas.wrap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;


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
		super(network, maxIters, exp, places);
	}

	/** Implement the Algorithm B version of bush equilibration
	 * @param b a bush to be equilibrated
	 * @param to the bush's topological order
	 * @throws Exception if there was negative bush flow
	 */
	protected synchronized void equilibrateBush(Bush b) {
//		network.acquireLocks();
		LinkedList<Node> to = b.getTopologicalOrder();
		Node cur;
		HashMap<Link, Double> deltaX = new HashMap<Link, Double>();
		b.shortTopoSearch();
		b.longTopoSearch();

		Iterator<Node> it = to.descendingIterator();
		while (it.hasNext()) {
			cur = it.next();
			if (cur.equals(b.getOrigin().getNode())) continue;


			AlternateSegmentPair asp = b.getShortLongASP(cur);
			if (asp == null || asp.getMaxDelta(deltaX) <= 0) continue;


			//Iterate through longest paths until reaching a node in shortest path

			//Reiterate through shortest path to build path up to divergence node
			//The two paths constitute a Pair of Alternate Segments

			//calculate delta h, capping at maxDelta
			Double denom = 0.0;
			for (Link l : asp.getShortPath()) denom += (l.pricePrime(b.getVOT()));			
			for (Link l : asp.getLongPath()) denom += (l.pricePrime(b.getVOT()));

			Double deltaH = Math.min(asp.getMaxDelta(deltaX),
					asp.getPriceDiff()/denom);

			
			//add delta h to all x values in pi_L
			for (Link l : asp.getShortPath()) {
				Double t = deltaX.getOrDefault(l, 0.0)+deltaH.doubleValue();

				deltaX.put(l, t.doubleValue());
			}

			//subtract delta h from all x values in pi_U
			for (Link l : asp.getLongPath()) {
				//				b.subtractFlow(l, deltaH);
				Double td = deltaX.getOrDefault(l, 0.0)-deltaH.doubleValue();
				Double ld = -l.getBushFlow(b);
				Double ud = Math.max(Math.ulp(ld),Math.ulp(td));

				if (td < ld) {
					if (ld-td <= 2*ud) {
						td = ld;
					}
					else throw new NegativeFlowException("too much bush flow removed: "+(ld-td)+"\tEps: "+ud);
				}
				if (td < -l.getFlow().doubleValue()) throw new NegativeFlowException("too much link flow removed");

				deltaX.put(l,td);
			}
		}

		for (Link z : new HashSet<Link>(deltaX.keySet())) {
			b.changeFlow(z, deltaX.get(z));
		}
		
	}

	@Override
	public String toString() {
		return "Algorithm B Optimizer";
	}

}
