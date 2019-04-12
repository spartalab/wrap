package edu.utexas.wrap.assignment;

import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.util.calc.BeckmannCalculator;
import edu.utexas.wrap.util.calc.GapCalculator;
import edu.utexas.wrap.util.calc.TSGCCalculator;
import edu.utexas.wrap.util.calc.TSTTCalculator;


/** Abstract method for optimizing link flows using an iterative
 * route choice algorithm. Calling the {@code optimize()} method
 * repeatedly iterates through the route choice algorithm until
 * the convergence criteria are met. After each iteration, the
 * statistics of the network are output.
 * @author William
 *
 */
public abstract class Optimizer {
	protected Integer iteration = 1;
	protected Integer maxIterations = 1000;

	protected final Graph graph;
	protected TSTTCalculator tc;
	protected TSGCCalculator cc;
	protected BeckmannCalculator bc;
	protected GapCalculator gc;
//	protected AECCalculator ac;

	public Optimizer(Graph g) {
		graph = g;
	}
	
	/** Stopping criterion - default is maximum number of iterations
	 * @return whether the optimizer has converged
	 */
	protected Boolean converged() {
		return iteration > maxIterations;
	}
	
	/**
	 * Perform a single optimization iteration
	 */
	protected abstract void iterate();

	/**
	 * Perform the iterative optimization algorithm, displaying
	 * network statistics after each iteration, until the
	 */
	public void optimize(){
		
		System.out.println("\r\n"
				+ "Iter. #\t"
				+ "AEC\t\t\t"
				+ "TSTT\t\t\t"
				+ "Beckmann\t\t"
				+ "Relative Gap\t\t"
				+ "TSGC\t\t\t"
				+ "Runtime");
		System.out.println(
				"----------------------------------"+
				"----------------------------------"+
				"----------------------------------"+
				"----------------------------------"
				);
		
		Long start = System.currentTimeMillis();
		Long end; Double runtime;
		
		do {
			System.out.print(iteration);
			iterate();
			System.out.print("\t"+getStatLine());
			
			end = System.currentTimeMillis();
			runtime = (end - start)/1000.0;
			System.out.println("\t"+String.format("%4.3f", runtime)+" s");
			
			iteration++;
			start = System.currentTimeMillis();

		} while (!converged());
		
	}
		
	/** Build a set of statistics about the network and combine them as a string
	 * TODO clean this shit up, future William! Love, present William, who is
	 * currently very saddened by past William's actions
	 * @return a string consisting of statistics about the current Graph
	 */
	protected String getStatLine() {
		String out = "";

		tc = new TSTTCalculator(graph);
		bc = new BeckmannCalculator(graph);
//		cc = new TSGCCalculator(graph, origins);
//		ac = new AECCalculator(this,cc);
//		gc = new GapCalculator(graph, origins,cc);

		tc.start();bc.start();//gc.start();cc.start();//ac.start();
		try {
			tc.join();bc.join();//gc.join();cc.join();//ac.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
//		out += String.format("%6.10E",ac.val) + "\t";
		out += "\t\t\t";
		
		out += String.format("%6.10E",tc.val) + "\t";
		out += String.format("%6.10E",bc.val) + "\t";
//		out += String.format("%6.10E",gc.val) + "\t";
//		out += String.format("%6.10E", cc.val);
	
		return out;
	}

	/**
	 * @param maxIterations the maximum number of iterations to be performed
	 */
	public void setMaxIterations(Integer maxIterations) {
		this.maxIterations = maxIterations;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public abstract String toString();
}
