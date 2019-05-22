package edu.utexas.wrap.assignment.bush;

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
	Integer numThreshold = 100;

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
		
		b.shortTopoSearch();
		b.longTopoSearch(true);
		Map<Link,Double> bushFlows = b.getFlows();
		
		Iterator<Node> it = to.descendingIterator();
		while (it.hasNext()) {
			cur = it.next();
			
			if (cur.equals(b.getOrigin().getNode())) continue; //break?
			
			Double md = b.getMaxDelta(cur, bushFlows);
			//TODO: streamline this inside bush structure
			AlternateSegmentPair asp = b.getShortLongASP(cur);
			if (asp == null || 
					md <= 0) 
				continue;

			//calculate delta h, capping at maxDelta
			Double deltaH = getDeltaH(asp, md);

			//Modify link flows
			updateDeltaX(asp, bushFlows, deltaH);
		}

		
	}

	private void updateDeltaX(AlternateSegmentPair asp, Map<Link,Double> flows, Double deltaH) {
		//add delta h to all x values in pi_L
		for (Link l : asp.shortPath()) {
			flows.put(l, flows.getOrDefault(l, 0.0)+deltaH);
			l.changeFlow(deltaH);
			if (deltaH > l.getFlow()) {
				throw new RuntimeException();
			}
		}

		//subtract delta h from all x values in pi_U
		for (Link l : asp.longPath()) {
			Double td = -deltaH.doubleValue();
			Double ld = -flows.get(l);
			Double ud = Math.max(Math.ulp(ld),Math.ulp(td));

			if (td < ld) {
				if (ld-td <= numThreshold*ud) {
					td = ld;
				}
				else throw new NegativeFlowException("Too much bush flow removed. Required threshold: "+(ld-td)/ud);
			}
			
			ld = -l.getFlow();
			ud = Math.max(Math.ulp(ld), Math.ulp(td));
			if (td < ld) {
				if (ld-td <= numThreshold*ud) {
					td = ld;
				}
				else throw new NegativeFlowException("Too much link flow removed. Required threshold: "+(ld-td)/ud);
			}
			
			Double newBushFlow = Math.min(flows.getOrDefault(l, 0.0)+td,l.getFlow()+td);
			flows.put(l, newBushFlow);
			if (flows.get(l) > l.getFlow()+td) {
				throw new RuntimeException();
			}
			l.changeFlow(td);

		}
		//TODO backtrack through bush and update splits
		asp.getBush().updateSplits(flows);
	}

	private Double getDeltaH(AlternateSegmentPair asp, Double md) {
		Double denom = 0.0;
		Float vot = asp.getBush().getVOT();
		for (Link l : asp.shortPath()) denom += (l.pricePrime(vot));			
		for (Link l : asp.longPath()) denom += (l.pricePrime(vot));

		return Math.min(md, asp.priceDiff()/denom);
	}

	@Override
	public String toString() {
		return "Algorithm B Optimizer";
	}

}
