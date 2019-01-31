package edu.utexas.wrap.assignment;

import java.util.List;

import edu.utexas.wrap.net.Node;

public abstract class Origin {
	private final Node self;
	
	public Origin(Node self) {
		//TODO figure out Zones
		this.self = self;
	}
	
	public abstract List<? extends AssignmentContainer> getContainers();
	
	
	public Node getNode() {
		return self;
	}
}
