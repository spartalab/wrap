package edu.utexas.wrap.assignment;

import java.io.IOException;

public interface AssignmentReader<C extends AssignmentContainer> {
	
	public void readStructure(C container) throws IOException;

}
