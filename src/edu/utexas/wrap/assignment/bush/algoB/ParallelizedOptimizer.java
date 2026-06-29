package edu.utexas.wrap.assignment.bush.algoB;

import edu.utexas.wrap.assignment.AssignmentContainer;
import edu.utexas.wrap.assignment.AssignmentOptimizer;
import edu.utexas.wrap.net.Graph;

/**
 * An {@link AssignmentOptimizer} that supports parallel processing of
 * individual containers. Each container can be optimized independently,
 * enabling concurrent bush processing across multiple threads.
 *
 * @param <T> the type of assignment container being optimized
 * @author Will Alexander
 * @see AlgorithmBOptimizer
 */
public interface ParallelizedOptimizer<T extends AssignmentContainer> 
			extends AssignmentOptimizer<T> {
	public void process(T container, Graph network);

}
