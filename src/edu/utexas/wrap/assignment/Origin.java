package edu.utexas.wrap.assignment;

import java.util.Collection;

import edu.utexas.wrap.net.Node;
import edu.utexas.wrap.net.TravelSurveyZone;

/**A point connected to the network from which trip demand emanates.
 * This point is associated with a Node that connects the demand to the
 * underlying network Graph. In practice, this acts as a  collection (List)
 * of AssignmentContainers associated with a specific demand origin point,
 * also known as a zone (although a zone may have multiple origins)
 * 
 * @author William
 *
 */
public abstract class Origin {
	//The network node with which the origin is associated
	private final Node self;
	
	/**
	 * @param self the Node from whence a set of assignment containers originate
	 */
	public Origin(TravelSurveyZone self) {
		this.self = self.getNode();
	}
	
	/**
	 * @return the assignment containers associated with this origin
	 */
	public abstract Collection<? extends AssignmentContainer> getContainers();
	
	
	/**
	 * @return the node with which the Origin is associated
	 */
	public Node getNode() {
		return self;
	}
	
}
