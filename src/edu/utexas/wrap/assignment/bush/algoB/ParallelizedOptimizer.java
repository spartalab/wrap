package edu.utexas.wrap.assignment.bush.algoB;

import edu.utexas.wrap.assignment.AssignmentContainer;
import edu.utexas.wrap.assignment.AssignmentOptimizer;
import edu.utexas.wrap.net.Graph;

public interface ParallelizedOptimizer<T extends AssignmentContainer> 
			extends AssignmentOptimizer<T> {
	public void process(T container, Graph network);

}
