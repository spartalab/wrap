package edu.utexas.wrap.assignment;

import java.util.stream.Stream;

public interface AssignmentOptimizer<T extends AssignmentContainer> {
	
	public void optimize(Stream<T> containerStream);

}
