package edu.utexas.wrap.assignment;

import java.io.IOException;

public interface AssignmentWriter<C extends AssignmentContainer> {

	public void writeStructure(C container) throws IOException;
}
