package edu.utexas.wrap.assignment.bush;

import edu.utexas.wrap.assignment.AssignmentConsumer;

public class BushForgetter implements AssignmentConsumer<Bush> {

	@Override
	public void consumeStructure(Bush container) {
		container.setQ(null);
	}

}
