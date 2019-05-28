package edu.utexas.wrap.assignment.bush;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.utexas.wrap.assignment.Optimizer;
import edu.utexas.wrap.net.Graph;
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
	private int innerIters = 8;
	protected Set<BushOrigin> origins;
	protected Integer relativeGapExp = -4;
	private boolean print = true;

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
	
	/* (non-Javadoc)
	 * @see edu.utexas.wrap.assignment.Optimizer#getStatLine()
	 */
	@Override
	protected String getStatLine() {
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
	
	/* (non-Javadoc)
	 * @see edu.utexas.wrap.assignment.Optimizer#iterate()
	 */
	public void iterate() {
		// A single general step iteration
		// TODO explore which bushes should be examined 
		
		ExecutorService p = Executors.newWorkStealingPool();
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
		while (!p.isTerminated());
		
		// Equilibrate bushes sequentially
		for (int i = 0; i < innerIters; i++) {
			for (BushOrigin o : origins) {
				for (Bush b : o.getContainers()) {
					equilibrateBush(b);
				}
			}
		}
		if (print) writeContainers();
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
	
	@Override
	public void writeContainers() {
		ExecutorService e = Executors.newWorkStealingPool();
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		for (byte b : graph.getMD5()) {
			sb.append(String.format("%02X", b));
		}
		for (BushOrigin o : origins) {
			for (Bush c : o.getContainers()) {
				Thread t = new Thread() {
					public void run() {
						
						File file = new File(sb+"/"+o.getNode().getID()+"/"+c.getVehicleClass()+"-"+c.getVOT()+".bush");
						file.getParentFile().mkdirs();
						try {
							PrintStream out = new PrintStream(file);
							
							c.toFile(out);

						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				};
				e.execute(t);
			}
		}
		e.shutdown();
		while (e.isTerminated());
	}
}
