package edu.utexas.wrap;

import java.util.Set;

public abstract class Optimizer {
	/**
	 * Maximum number of decimal places past zero that 
	 * links should care about for flow values. Default
	 * rounding mode is RoundingMode.HALF_EVEN
	 */
	private Integer iteration = 1;
	protected Integer maxIterations = 1000;
	protected Integer relativeGapExp = -6;

	protected final Graph graph;
	protected final Set<Origin> origins;
	private TSTTCalculator tc;
	private TSGCCalculator cc;
	private BeckmannCalculator bc;
	private GapCalculator gc;
//	private AECCalculator ac;

	public Optimizer(Graph g, Set<Origin> o) {
		graph = g;
		origins = o;
	}
	
	private Boolean converged() {
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
	
	public abstract void iterate();

	public void optimize(){
		
		System.out.println();
		System.out.println("Iter. #\tAEC\t\t\tTSTT\t\t\tBeckmann\t\tRelative Gap\t\tTSGC\t\t\tRuntime");
		System.out.println("----------------------"+
				"---------------------------------"+
				"---------------------------------"+
				"---------------------------------"+
				"--------------");
		
		Long start = System.currentTimeMillis();
		Long end; Double runtime;
		
		do {
			System.out.print(iteration);
			iterate();
			System.out.print("\t"+getStats());
			
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
		
	private String getStats() {
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

	public void setMaxIterations(Integer maxIterations) {
		this.maxIterations = maxIterations;
	}
	
	public void setRelativeGapExp(Integer relativeGapExp) {
		this.relativeGapExp = relativeGapExp;
	}
	
	public abstract String toString();
}
