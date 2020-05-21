package edu.utexas.wrap.assignment;

import java.util.Collection;

import edu.utexas.wrap.demand.ODMatrix;

public interface AssignmentInitializer<T extends AssignmentContainer> {
	
	public Collection<T> initializeContainers();
	
	public void add(ODMatrix matrix);
}
