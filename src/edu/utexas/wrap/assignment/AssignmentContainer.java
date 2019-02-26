package edu.utexas.wrap.assignment;

import java.util.Set;

import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.Node;

/**A label indicating an object may be associated with a link flow in route 
 * choice. Examples include bushes and paths for bush- and path-based 
 * optimizers, respectively.
 * 
 * @author William
 *
 */
public interface AssignmentContainer {

	public Mode getVehicleClass();
	
	public Float getVOT();

	public Float getDemand(Node n);
	
	public Set<Link> getLinks();

	public Double getFlow(Link l);
	
}
