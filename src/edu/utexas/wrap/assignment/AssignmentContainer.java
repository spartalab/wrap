package edu.utexas.wrap.assignment;

import edu.utexas.wrap.VehicleClass;
import edu.utexas.wrap.net.Node;

/**A label indicating an object may be associated with a link flow.
 * Examples include bushes and paths for bush- and path-based optimizers,
 * respectively.
 * @author William
 *
 */
public interface AssignmentContainer {

	public VehicleClass getVehicleClass();
	
	public Float getVOT();

	public Float getDemand(Node n);
	
}
