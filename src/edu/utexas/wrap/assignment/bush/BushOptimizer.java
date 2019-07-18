package edu.utexas.wrap.assignment.bush;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.stream.Collectors;

import edu.utexas.wrap.assignment.Optimizer;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.util.calc.AECCalculator;
import edu.utexas.wrap.util.calc.BeckmannCalculator;
import edu.utexas.wrap.util.calc.GapCalculator;
import edu.utexas.wrap.util.calc.LowestCostPathCostCalculator;
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
	public static boolean printProgress = false;
	public static boolean printBushes = false;

	private int innerIters = 8;
	
	protected Set<BushOrigin> origins;
	protected Integer relativeGapExp = -8;

	/**Default constructor
	 * @param g	the graph on which the optimizer should operate
	 * @param o	the set of BushOrigins to equilibrate
	 */
	public BushOptimizer(Graph g, Set<BushOrigin> o) {
		super(g);
		origins = o;
	}

	/**the method of equilibration
	 * @param b the Bush to be brought closer to equilibrium
	 */
	protected abstract void equilibrateBush(Bush b);
	
	/* (non-Javadoc)
	 * @see edu.utexas.wrap.assignment.Optimizer#converged()
	 */
	@Override
	protected Boolean converged() {
		try {
			if (iteration > maxIterations) return true;
			//Check the relative gap against a given value
			if (gc == null) {
				gc = new GapCalculator(graph, origins, null, null);
				gc.start();
				gc.join();
			}
			return gc.val < Math.pow(10, relativeGapExp);
		} catch (Exception e) {
			e.printStackTrace();
			return iteration > maxIterations;
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.utexas.wrap.assignment.Optimizer#getStatLine()
	 */
	@Override
	protected String getStatLine() {
		StringBuilder out = new StringBuilder();

		tc = new TSTTCalculator(graph);
		bc = new BeckmannCalculator(graph);
		cc = new TSGCCalculator(graph, origins);
		lc = new LowestCostPathCostCalculator(graph, origins);
		ac = new AECCalculator(graph,origins,cc,lc);
		gc = new GapCalculator(graph, origins,cc,lc);

		cc.start();
		tc.start();
		lc.start();
		gc.start();
		bc.start();
		ac.start();
		try {
			tc.join();
			gc.join();
			lc.join();
			bc.join();
			cc.join();
			ac.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		out.append(String.format("%6.10E",ac.val) + "\t");
//		out.append("\t\t\t");
		out.append(String.format("%6.10E",tc.val) + "\t");
		out.append(String.format("%6.10E",bc.val) + "\t");
		out.append(String.format("%6.10E",gc.val) + "\t");
		out.append(String.format("%6.10E", cc.val));
	
		return out.toString();
	}
	
	/* (non-Javadoc)
	 * @see edu.utexas.wrap.assignment.Optimizer#iterate()
	 */
	public void iterate() {
		// A single general step iteration
		// TODO explore which bushes should be examined 
		Long imprStart = System.currentTimeMillis();
		ForkJoinPool p = (ForkJoinPool) Executors.newWorkStealingPool();
		for (BushOrigin o : origins) {
			for (Bush b : o.getContainers()) {
				Thread t = new Thread() {
					public void run() {
						//Improve bushes in parallel
						b.improve();
					}
				};
				p.execute(t);
			}
		}

		//Wait until all bushes have finished improving
		p.shutdown();
		while (!p.isTerminated()) {
			if (shuttingDown) p.shutdownNow();
			if (printProgress) try {
				System.out.print("\tImproving bushes\t"
						+ "In queue: "+String.format("%1$5s",p.getQueuedSubmissionCount())+"\t"
						+ "Active: "+p.getActiveThreadCount()+"\t"
						+ "Memory usage: "+(Runtime.getRuntime().totalMemory()/1048576)+" MiB\t\r");
				Thread.sleep(250);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		int numZones = origins.size();
		// Equilibrate bushes sequentially
		//TODO redesign status visualization
		Long start, end; 

		outer: for (int i = 0; i < innerIters; i++) {
			int j = 1;
			start = imprStart != null? imprStart : System.currentTimeMillis();
			for (BushOrigin o : origins) {
				int numBushes = o.getContainers().size();

				int k = 1;
				for (Bush b : o.getContainers()) {
					if (shuttingDown) break outer;
					if (printProgress) System.out.print("Equilibration "+(i+1)+" out of "+innerIters+"\t"
							+ "Origin "+String.format("%1$5s", j)+" out of "+numZones+"\t"
							+ "Bush "+String.format("%1$2s", k)+" out of "+String.format("%1$2s", numBushes)+"\r");
					
					equilibrateBush(b);
					if (printBushes) b.toDefaultFile().start();
					k++;
				}
				j++;
			}
			end = System.currentTimeMillis();
			imprStart = null;
			//Measure stats of the network and write to terminal
			System.out.print(iteration+"\t"+getStatLine());
			ttime += end-start;
			System.out.println("\t"+String.format("%4.3f", ttime/1000.0));
		}
	}
	
	/**Set how many inner equilibrations should be performed
	 * @param innerIters the number of times the bushes should be equilibrated before improving
	 */
	public void setInnerIters(int innerIters) {
		this.innerIters = innerIters;
	}
	
	/**
	 * @param relativeGapExp the relative gap exponent that should signal convergence
	 */
	public void setRelativeGapExp(Integer relativeGapExp) {
		this.relativeGapExp = relativeGapExp;
	}
	
	public void checkFlows() {
		Map<Link,Double> linkFlows = graph.getLinks().parallelStream().collect(Collectors.toMap(Function.identity(), l -> l.getFlow()));
		Map<Link,Double> bushFlows = new HashMap<Link,Double>(graph.numLinks(),1.0f);
		
		origins.parallelStream().flatMap(o -> o.getContainers().parallelStream()).map(b -> b.getFlows()).sequential().forEach(m ->{
			for (Entry<Link, Double> e : m.entrySet()) {
				bushFlows.put(e.getKey(), bushFlows.getOrDefault(e.getKey(), 0.0) + e.getValue());
			}
		});;
		
		for (Entry<Link,Double> e : linkFlows.entrySet()) {
			double total = bushFlows.getOrDefault(e.getKey(),0.0); 
			if (total - e.getValue() > 20*Math.max(Math.ulp(total), Math.ulp(e.getValue()))) 
				throw new RuntimeException();
		}
	}
	
}
