package edu.utexas.wrap.assignment;

import java.util.Collection;

import edu.utexas.wrap.gui.IteratorRunner;
import edu.utexas.wrap.net.Graph;

/**
 * An {@link AssignmentOptimizer} that performs optimization in discrete,
 * countable iterations. Each call to {@link #iterate} processes all containers
 * once, allowing external control over the iteration loop (e.g., for GUI
 * progress tracking or inter-iteration evaluation).
 *
 * @param <T> the type of assignment container being optimized
 * @author Will Alexander
 */
public interface AtomicOptimizer<T extends AssignmentContainer> 
			extends AssignmentOptimizer<T>{

	/**
	 * Performs a single optimization iteration over all containers.
	 *
	 * @param containers the collection of assignment containers to optimize
	 * @param network the transportation network graph
	 * @param runner the iterator runner controlling execution and progress
	 */
	public void iterate(
			Collection<T> containers,
			Graph network,
			IteratorRunner<T> runner
			);

	/**
	 * Sets the number of iterations that have already been completed,
	 * typically used when resuming from a saved state.
	 *
	 * @param i the number of previously completed iterations
	 */
	public void setCompletedIterations(int i);

}
