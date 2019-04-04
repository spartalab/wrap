package edu.utexas.wrap.assignment.bush;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.utexas.wrap.assignment.Optimizer;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.Node;
import edu.utexas.wrap.util.UnreachableException;
import edu.utexas.wrap.util.calc.BeckmannCalculator;
import edu.utexas.wrap.util.calc.GapCalculator;
import edu.utexas.wrap.util.calc.TSGCCalculator;
import edu.utexas.wrap.util.calc.TSTTCalculator;

/**Assignment optimizer using bush techniques. Includes a
 * method to "improve" bushes by expanding to include links that
 * provide a shortcut and removing unneeded links. The method of
 * equilibrating flows is left abstract.
 * @author William
 *
 */
public abstract class BushOptimizer extends Optimizer {
	private int innerIters = 10;
	protected Set<BushOrigin> origins;

	public BushOptimizer(Graph g, Set<BushOrigin> o) {
		super(g);
		origins = o;
	}

	
	protected abstract void equilibrateBush(Bush b);
	
	@Override
	protected Boolean converged() {
		try {
			if (iteration > maxIterations) return true;
			if (gc == null) {
				gc = new GapCalculator(graph, origins, null);
				gc.start();
				gc.join();
			}
			return gc.val < Math.pow(10, relativeGapExp);
		} catch (Exception e) {
			e.printStackTrace();
			return iteration > maxIterations;
		}
	}
	
	@Override
	protected String getStats() {
		String out = "";

		tc = new TSTTCalculator(graph);
		bc = new BeckmannCalculator(graph);
		cc = new TSGCCalculator(graph, origins);
//		ac = new AECCalculator(this,cc);
		gc = new GapCalculator(graph, origins,cc);

		cc.start();tc.start();gc.start();bc.start();//ac.start();
		try {
			tc.join();gc.join();bc.join();cc.join();//ac.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
//		out += String.format("%6.10E",ac.val) + "\t";
		out += "\t\t\t";
		
		out += String.format("%6.10E",tc.val) + "\t";
		out += String.format("%6.10E",bc.val) + "\t";
		out += String.format("%6.10E",gc.val) + "\t";
		out += String.format("%6.10E", cc.val);
	
		return out;
	}
	
	protected Boolean improveBush(Bush b) {
		//TODO cleanup, move to Bush

		b.prune();

		boolean modified = false;
		Set<Link> usedLinks = b.getLinks();
		Set<Link> unusedLinks = new HashSet<Link>(graph.getLinks());
		unusedLinks.removeAll(usedLinks);
		
		Map<Node, Double> cache = b.longTopoSearch();
		
		for (Link l : unusedLinks) {
			// If link is active, do nothing (removing flow should mark as inactive)
			//Could potentially delete both incoming links to a node
			if (!l.allowsClass(b.getVehicleClass()) || !b.isValidLink(l)) continue;
			try {
				// Else if Ui + tij < Uj
				
				Double tailU = b.getCachedU(l.getTail(), cache);
				Double headU = b.getCachedU(l.getHead(), cache);
			
				
				if (tailU + (l.getPrice(b.getVOT(),b.getVehicleClass())) < headU) {
					b.activate(l);
					modified = true;
				}
			} catch (UnreachableException e) {
				if (e.demand > 0) e.printStackTrace();
				continue;
			}

		}

		return modified;
	}

	public void iterate() {
		// A single general step iteration
		// TODO explore which bushes should be examined 
		
		Set<Thread> pool = new HashSet<Thread>();
		
		for (BushOrigin o : origins) {
			for (Bush b : o.getBushes()) {
				Thread t = new Thread() {
					public void run() {
						// Step ii: Improve bushes in parallel
						improveBush(b);
					}
				};
				pool.add(t);
				t.start();
			}
		}
		try {
			for (Thread t : pool) {
				t.join();

			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < innerIters; i++) {
			for (BushOrigin o : origins) {
				for (Bush b : o.getBushes()) {
					// Step i: Equilibrate bushes sequentially
					equilibrateBush(b);
				}
			}
		}
	}
	
	public void setInnerIters(int innerIters) {
		this.innerIters = innerIters;
	}

}
