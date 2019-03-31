package edu.utexas.wrap.demand;

import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;

/**A map from an origin-destination pair to the number
 * of vehicle-trips made. This is different from the PA
 * matrix in that it may account for multiple person-trips
 * inside a single vehicle-trip, i.e. passengers in a car
 * or bus.
 * 
 * @author William
 *
 */
public interface ODMatrix {
	
	public Mode getMode();

	public Float getDemand(Node origin, Node destination);
	
	public void put(Node origin, Node destination, Float demand);

	public Graph getGraph();
	
}
