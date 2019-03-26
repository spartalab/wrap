package edu.utexas.wrap.demand;

import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Node;

/**This is used to show the number of trips
 * between an origin and destination. This component
 * is generated after trip generation.
 *
 * @author William
 *
 */
public interface ModalODMatrix extends ODMatrix {
	public Mode getMode();

	public void put(Node origin, Node destination, Float demand);

}
