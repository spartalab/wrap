package edu.utexas.wrap.assignment;

import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.util.calc.BeckmannCalculator;
import edu.utexas.wrap.util.calc.AllPathsRelativeGapCalculator;
import edu.utexas.wrap.util.calc.LowestCostPathCostCalculator;
import edu.utexas.wrap.util.calc.TotalSystemGeneralizedCostCalculator;
import edu.utexas.wrap.util.calc.TotalSystemTravelTimeCalculator;
import edu.utexas.wrap.util.calc.AverageExcessCostCalculator;


/** Abstract method for optimizing link flows using an iterative
 * route choice algorithm. Calling the {@code optimize()} method
 * repeatedly iterates through the route choice algorithm until
 * the convergence criteria are met. After each iteration, the
 * statistics of the network are output.
 * @author William
 *
 */
@Deprecated
public abstract class OldOptimizer {
	//Loop control variables
	public boolean shuttingDown = false;
	protected Integer iteration = 1;
	protected Integer maxIterations = 1000;

	protected final Graph graph;
	//Measurement utils
	protected TotalSystemTravelTimeCalculator tc;
	protected TotalSystemGeneralizedCostCalculator cc;
	protected BeckmannCalculator bc;
	protected AllPathsRelativeGapCalculator gc;
	protected AverageExcessCostCalculator ac;
	protected LowestCostPathCostCalculator lc;
	protected long ttime;
	public OldOptimizer(Graph g) {
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
		
		//TODO: build a better network stat interface
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
		
//		Long start, end; Double runtime;
		ttime = 0L;
		do {
			
			if (shuttingDown) break;
			//Perform a full iteration, measuring performance time
//			start = System.currentTimeMillis();
			iterate();
//			end = System.currentTimeMillis();
			
			if (shuttingDown) break;
			
			//Measure stats of the network and write to terminal
//			System.out.print(iteration+"\t"+getStatLine());
//			runtime = (end - start)/1000.0;
//			System.out.println("\t"+String.format("%4.3f", runtime)+" s");
			
			iteration++;
		
		//Check for convergence
		} while (!converged());
		
	}
		
	/** Build a set of statistics about the network and combine them as a string
	 * Should return the following stats in order: AEC, TSTT, Beckmann, Relative Gap, TSGC
	 * @return a string consisting of statistics about the current Graph
	 */
	abstract protected String getStatLine();

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
