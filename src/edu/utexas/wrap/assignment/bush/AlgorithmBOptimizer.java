package edu.utexas.wrap.assignment.bush;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.Node;
import edu.utexas.wrap.util.AlternateSegmentPair;
import edu.utexas.wrap.util.NegativeFlowException;


/** This class implements Dial's Algorithm B to equilibrate
 * bushes as a bush-based optimizer
 * @author William
 *
 */
public class AlgorithmBOptimizer extends BushOptimizer{
	Integer numThreshold = 100;

	/**
	 * @param g the graph on which the Optimizer should operate 
	 * @param o the set of origins to optimize/equilibrate
	 */
	public AlgorithmBOptimizer(Graph g, Set<BushOrigin> o) {
		super(g,o);
	}

	/** Implement the Algorithm B version of bush equilibration
	 * @param b a bush to be equilibrated
	 */
	protected synchronized void equilibrateBush(Bush b) {
		LinkedList<Node> to = b.getTopologicalOrder();
		Node cur;
		
		//Assign the correct back-pointers to BushMerges using topological search
		b.shortTopoSearch();
		b.longTopoSearch(true);
		//Get the flows on the current bush
		Map<Link,Double> bushFlows = b.getFlows();
		
		//In reverse topological order,
		Iterator<Node> it = to.descendingIterator();
		while (it.hasNext()) {
			cur = it.next();
			
			if (cur.equals(b.getOrigin().getNode())) continue; //Ignore the origin. Should be same as break if topoOrder is correct 
			
			//Determine the maximum delta that can be shifted
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

	/** Apply link flow changes on an AlternateSegmentPair
	 * @param asp the pair of long and short links to modify
	 * @param flows the current flows on the bush
	 * @param deltaH the amount by which flows should be changed
	 */
	private void updateDeltaX(AlternateSegmentPair asp, Map<Link,Double> flows, Double deltaH) {
		//add delta h to all x values in the shortest path
		for (Link l : asp.shortPath()) {
			flows.put(l, flows.getOrDefault(l, 0.0)+deltaH); //Modify bush flow
			l.changeFlow(deltaH);	//Modify total flow on link
			
			if (deltaH > l.getFlow()) {
				throw new RuntimeException("Link flow was already negative?? This shouldn't happen");
			}
		}

		//subtract delta h from all x values in longest path
		for (Link l : asp.longPath()) {
			//This next section handles some numerical instability
			Double td = -deltaH.doubleValue();	//Current amount to be subtracted
			Double ld = -flows.get(l);	//Maximum amount that can be subtracted
			Double ud = Math.max(Math.ulp(ld),Math.ulp(td)); //Machine epsilon on these two terms (lowest precision available)

			if (td < ld) { //If too much flow is removed from the bush
				if (ld-td <= numThreshold*ud) {	//See if it's within the numerical tolerance
					td = ld;	//Cap at the smaller amount if so
				}
				else throw new NegativeFlowException("Too much bush flow removed. Required threshold: "+(ld-td)/ud);
			}
			
			//Now do the same thing, but this time for link flow
			ld = -l.getFlow();	//Maximum amount that can be subtracted
			ud = Math.max(Math.ulp(ld), Math.ulp(td));	//Machine epsilon on the two terms (lowest precision available)
			if (td < ld) {	//If too much flow is removed from the bush
				if (ld-td <= numThreshold*ud || (ld-td) < Math.pow(10, -4)) {	//See if it's within the numerical tolerance
					td = ld;	//Cap at the smaller amount if so
				}
				else throw new NegativeFlowException("Too much link flow removed. "
						+ "Required threshold: "+(ld-td)/ud+"\tLink flow: "+ld
						+ "\tdelta H: "+td);
			}
			
			//Safeguard cap at the smaller of the two to ensure bush flow never exceeds link flow
			Double newBushFlow = Math.min(flows.getOrDefault(l, 0.0)+td,l.getFlow()+td);
			flows.put(l, newBushFlow);
			if (flows.get(l) > l.getFlow()+td) {
				throw new RuntimeException("Bush flow larger than link flow");
			}
			//Modify the total link flow
			l.changeFlow(td);
		}
		
		//backtrack through bush and update splits
		asp.getBush().updateSplits(flows);
	}

	/** Calculate the amount of flow to shift. Delta H is defined as the
	 * difference in the ASP's longest and shortest path costs divided by
	 * the sum of the first derivatives of the costs for all links in the ASP
	 * @param asp	The AlternateSegmentPair used to calculate delta H
	 * @param md	The maximum delta allowed
	 * @return		The smaller of the calculated delta H or the max delta
	 */
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
