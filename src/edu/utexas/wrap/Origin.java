package edu.utexas.wrap;

import java.util.List;

import edu.utexas.wrap.assignment.AssignmentContainer;
import edu.utexas.wrap.net.Node;

public abstract class Origin {
	protected final Node self;
	protected List<? extends AssignmentContainer> containers;
	
	public Origin(Node self) {
		this.self = self;
	}
	
	public abstract List<? extends AssignmentContainer> getContainers();
	
	
	public Node getNode() {
		return self;
	}
}
