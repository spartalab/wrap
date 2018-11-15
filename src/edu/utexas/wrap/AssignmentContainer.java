package edu.utexas.wrap;

/**A label indicating an object may be associated with a link flow.
 * Examples include bushes and paths for bush- and path-based optimizers,
 * respectively.
 * @author William
 *
 */
public interface AssignmentContainer {

	public VehicleClass getVehicleClass();
	
	public Float getVOT();
	
}
