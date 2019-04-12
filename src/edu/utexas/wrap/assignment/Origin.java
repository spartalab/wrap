package edu.utexas.wrap.assignment;

import java.util.List;

import edu.utexas.wrap.net.Node;

/**A point connected to the network from which 
 * trip demand emanates. This point is associated
 * with a Node that connects the demand to the underlying
 * network Graph. In practice, this acts as a  collection (List)
 * of AssignmentContainers associated with a specific 
 * demand origin point
 * 
 * TODO modify this to extend a collection of AssignmentContainers
 * rather than just having it.
 * 
 * @author William
 *
 */
public abstract class Origin {
	private final Node self;
	
	/**
	 * @param self the Node from whence a set of assignment containers originate
	 */
	public Origin(Node self) {
		//TODO figure out Zones
		this.self = self;
	}
	
	/**
	 * @return the assignment containers associated with this origin
	 */
	public abstract List<? extends AssignmentContainer> getContainers();
	
	
	/**
	 * @return the node with which the Origin is associated
	 */
	public Node getNode() {
		return self;
	}
}
