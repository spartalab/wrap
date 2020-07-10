package edu.utexas.wrap.assignment;

import java.io.IOException;

public interface AssignmentConsumer<C extends AssignmentContainer> {

	public void consumeStructure(C container) throws IOException;
}
