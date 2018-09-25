package edu.utexas.wrap;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.math.MathContext;

public abstract class Optimizer {
	private Integer iteration = 1;
	protected final Integer maxIterations;
	protected final Integer relativeGapExp;
	protected final Network network;
	/**
	 * Maximum number of decimal places past zero that 
	 * links should care about for flow values. Default
	 * rounding mode is RoundingMode.HALF_EVEN
	 */
	static Integer decimalPlaces = 16;
	static MathContext defMC = MathContext.DECIMAL64;
	
	public Optimizer(Network network) {
		this(network, 1000);
	}
	
	public Optimizer(Network network, Integer maxIters) {
		this(network, maxIters, -6);
	}
	
	public Optimizer(Network network, Integer maxIters, Integer exp) {
		this(network, maxIters, exp, 16);
	}

	public Optimizer(Network network, Integer maxIters, Integer exp, Integer places) {
		this.network	= network;
		maxIterations	= maxIters;
		relativeGapExp	= exp;
		decimalPlaces	= places;
	}
	
	public Network getNetwork() {
		return network;
	}

	public abstract void iterate();
		
	public abstract String toString();
	
	public void optimize(){
		
		System.out.println();
		System.out.println("Iter. #\tAEC\t\t\tTSTT\t\t\tBeckmann\t\tRelative Gap\t\tRuntime");
		System.out.println("---------------------------------------------------------------------------------------------------------------");
		
		Long start = System.currentTimeMillis();
		Long end; Double runtime;
		
		
		do {
			network.clearCache();
			System.out.print(iteration);
			iterate();
			System.out.print("\t"+network.toString());
			
			end = System.currentTimeMillis();
			runtime = (end - start)/1000.0;
			System.out.println("\t"+String.format("%4.3f", runtime)+" s");
			
//			if (wrap.printFlows) try {
//				network.printFlows(new PrintStream("flows-iter-"+iteration+".txt"));
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			}
			iteration++;
			start = System.currentTimeMillis();

		} while (!converged());
		
	}
	
	private Boolean converged() {
		try {
			
			return iteration > maxIterations || network.relativeGap() < Math.pow(10, relativeGapExp);
		} catch (Exception e) {
			e.printStackTrace();
			return iteration > maxIterations;
		}
	}
}
