package edu.utexas.wrap.assignment;

import java.util.Collection;

import edu.utexas.wrap.gui.IteratorRunner;
import edu.utexas.wrap.net.Graph;

public interface AtomicOptimizer<T extends AssignmentContainer> 
			extends AssignmentOptimizer<T>{

	public void iterate(
			Collection<T> containers,
			Graph network,
			IteratorRunner<T> runner
			);

}
