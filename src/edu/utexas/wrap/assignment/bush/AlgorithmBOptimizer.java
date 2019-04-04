package edu.utexas.wrap.assignment.bush;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.Node;
import edu.utexas.wrap.util.AlternateSegmentPair;
import edu.utexas.wrap.util.NegativeFlowException;


public class AlgorithmBOptimizer extends BushOptimizer{

	public AlgorithmBOptimizer(Graph g, Set<BushOrigin> o) {
		super(g,o);
	}

	/** Implement the Algorithm B version of bush equilibration
	 * @param b a bush to be equilibrated
	 * @param to the bush's topological order
	 * @throws Exception if there was negative bush flow
	 */
	protected synchronized void equilibrateBush(Bush b) {
		LinkedList<Node> to = b.getTopologicalOrder();
		Node cur;
		HashMap<Link, Double> deltaX = new HashMap<Link, Double>();
		
		b.shortTopoSearch();
		b.longTopoSearch();
		Map<Link,Double> flows = b.getFlows();
		
		Iterator<Node> it = to.descendingIterator();
		while (it.hasNext()) {
			cur = it.next();
			if (cur.equals(b.getOrigin().getNode())) continue;


			AlternateSegmentPair asp = b.getShortLongASP(cur);
			if (asp == null || asp.maxDelta(deltaX,flows) <= 0) continue;

			//calculate delta h, capping at maxDelta
			Double deltaH = getDeltaH(asp, b, flows, deltaX);

			//Modify link flows
			updateDeltaX(asp, b, flows, deltaX, deltaH);
		}

		for (Link z : new HashSet<Link>(deltaX.keySet())) {
			b.changeFlow(z, deltaX.get(z), flows);
		}
		
	}

	private void updateDeltaX(AlternateSegmentPair asp, Bush b, Map<Link,Double> flows, HashMap<Link, Double> deltaX, Double deltaH) {
		//add delta h to all x values in pi_L
		for (Link l : asp.shortPath()) {
			Double t = deltaX.getOrDefault(l, 0.0)+deltaH.doubleValue();

			deltaX.put(l, t.doubleValue());
		}

		//subtract delta h from all x values in pi_U
		for (Link l : asp.longPath()) {
			//				b.subtractFlow(l, deltaH);
			Double td = deltaX.getOrDefault(l, 0.0)-deltaH.doubleValue();
			Double ld = -flows.get(l);
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

	private Double getDeltaH(AlternateSegmentPair asp, Bush b, Map<Link,Double> flows, Map<Link, Double> deltaX ) {
		Double denom = 0.0;
		for (Link l : asp.shortPath()) denom += (l.pricePrime(b.getVOT()));			
		for (Link l : asp.longPath()) denom += (l.pricePrime(b.getVOT()));

		return Math.min(asp.maxDelta(deltaX,flows), asp.priceDiff()/denom);
	}

	@Override
	public String toString() {
		return "Algorithm B Optimizer";
	}

}
