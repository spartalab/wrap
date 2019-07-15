package edu.utexas.wrap.assignment.bush;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.Node;
import edu.utexas.wrap.util.NegativeFlowException;
import edu.utexas.wrap.util.UnreachableException;


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
		try {
			//Wait for the bush to be idle (it may be being written to a file)
			b.acquire();
			//Acquire the topological order of the bush and cahce for later use
			b.clearLabels();
			Node[] to = b.getTopologicalOrder(true);
			Node cur;

			//Assign the correct back-pointers to BushMerges using topological search
			b.shortTopoSearch();
			b.longTopoSearch(true);
			
			//Get the flows on the current bush
			Map<Link,Double> bushFlows = b.getFlows();

			//In reverse topological order,
			for (int i = to.length - 1; i >= 0; i--) {
				cur = to[i];
				
				//Ignore the origin. Should be same as break if topoOrder is correct 
				if (cur == null || cur.equals(b.getOrigin().getNode())) continue;

				//Determine the ASP, including the maximum delta that can be shifted
				AlternateSegmentPair asp = b.getShortLongASP(cur, bushFlows);
				if (asp == null) continue; //No ASP exists

				//calculate delta h, capping at maxDelta
				Double deltaH = getDeltaH(asp);

				//Modify link flows
				updateDeltaX(asp, bushFlows, deltaH);
			}

			//Update bush merge splits for next flow calculation
			b.updateSplits(bushFlows);
			//Release cached topological order memory for later use
			b.clearCache();
			//The bush can now be written or improved
			b.release();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnreachableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/** Apply link flow changes on an AlternateSegmentPair
	 * @param asp the pair of long and short links to modify
	 * @param flows the current flows on the bush
	 * @param deltaH the amount by which flows should be changed
	 */
	private void updateDeltaX(AlternateSegmentPair asp, Map<Link,Double> flows, Double deltaH) {
		//add delta h to all x values in the shortest path
		StreamSupport.stream(asp.shortPath().spliterator(), true).unordered().forEach(l ->{
			flows.put(l, flows.getOrDefault(l, 0.0)+deltaH); //Modify bush flow
			l.changeFlow(deltaH);	//Modify total flow on link

			if (deltaH > l.getFlow()) {
				throw new RuntimeException("Link flow was already negative?? This shouldn't happen");
			}
		});

		//subtract delta h from all x values in longest path
		StreamSupport.stream(asp.longPath().spliterator(), true).unordered().forEach(l->{
			Double safeDeltaH = numericalGuard(l, flows, deltaH);

			//Safeguard cap at the smaller of the two to ensure bush flow never exceeds link flow
			Double newBushFlow = Math.min(flows.getOrDefault(l, 0.0),l.getFlow())+safeDeltaH;
			if (newBushFlow > l.getFlow()+safeDeltaH) {
				throw new RuntimeException("Bush flow larger than link flow");
			}
			flows.put(l, newBushFlow);
			//Modify the total link flow
			l.changeFlow(safeDeltaH);
		});
	}

	/**Given an amount to shift on a link and the bush flows available, calculate
	 * the numerically safe amount that can be removed.
	 * @param l
	 * @param bushFlows
	 * @param deltaH
	 * @return
	 */
	private Double numericalGuard(Link l, Map<Link, Double> bushFlows, Double deltaH) {
		//This next section handles some numerical instability
		if (deltaH == null || deltaH == 0.0) return 0.0;
		Double td = -deltaH;	//Current amount to be subtracted
		Double ld = -bushFlows.getOrDefault(l,0.0);	//Maximum amount that can be subtracted
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
			if (ld-td <= numThreshold*ud || (ld-td) < Math.pow(10, -3)) {	//See if it's within the numerical tolerance
				td = ld;	//Cap at the smaller amount if so
			}
			else throw new NegativeFlowException("Too much link flow removed. "
					+ "Required threshold: "+(ld-td)/ud+"\tLink flow: "+ld
					+ "\tdelta H: "+td);
		}
		return td;
	}

	/** Calculate the amount of flow to shift. Delta H is defined as the
	 * difference in the ASP's longest and shortest path costs divided by
	 * the sum of the first derivatives of the costs for all links in the ASP
	 * @param asp	The AlternateSegmentPair used to calculate delta H
	 * @return		The smaller of the calculated delta H or the max delta
	 */
	private Double getDeltaH(AlternateSegmentPair asp) {
		Float vot = asp.getBush().getVOT();
		
		Double denominator = Stream.concat(
				//for all links in the longest and shortest paths
				StreamSupport.stream(asp.shortPath().spliterator(), true),
				StreamSupport.stream(asp.longPath().spliterator(), true)
				//In no particular order, sum the price derivatives
				).unordered().mapToDouble(x -> x.pricePrime(vot)).sum();

		//cut off at the maximum delta
		return Math.min(asp.maxDelta(), asp.priceDiff()/denominator);
	}

	@Override
	public String toString() {
		return "Algorithm B Optimizer";
	}

}
