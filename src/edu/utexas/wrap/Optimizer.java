package edu.utexas.wrap;

public abstract class Optimizer {
	private Integer iteration = 1;
	private final Integer maxIterations;
	private final Integer relativeGapExp = -6;
	protected final Network network;
	/**
	 * Maximum number of decimal places past zero that 
	 * links should care about for flow values. Default
	 * rounding mode is RoundingMode.HALF_EVEN
	 */
	static Integer decimalPlaces = 16;
	
	public Optimizer(Network network) {
		this(network, 1000);
	}
	
	public Optimizer(Network network, Integer maxIters) {
		this.network = network;
		maxIterations = maxIters;
	}

	public Network getNetwork() {
		return network;
	}

	public abstract void iterate();
		
	public abstract String toString();
	
	public void optimize(){
		
		System.out.println();
		System.out.println("ITERATION #\tAEC\t\t\tTSTT\t\t\tBeckmann\t\tRelative Gap");
		System.out.println("-------------------------------------------------------------------------------------------------------------");
		
		Long start = System.currentTimeMillis();
		do {
			System.out.print("Iteration "+iteration);
			iterate();
			System.out.print("\t"+network.toString()+"\r");
			iteration++;
		} while (!converged());
		Long end = System.currentTimeMillis();
		
		Double runtime = (end - start)/1000.0;
		System.out.println("Runtime "+runtime+" seconds");
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
