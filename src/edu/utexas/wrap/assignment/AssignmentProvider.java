package edu.utexas.wrap.assignment;

import java.io.IOException;

public interface AssignmentProvider<C extends AssignmentContainer> {
	
	public void getStructure(C container) throws IOException;

}
