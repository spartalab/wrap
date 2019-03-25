package edu.utexas.wrap.demand;

import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;

/**A simple map from a node to a demand level
 * @author William
 *
 */
public interface DemandMap {

	public Float get(Node dest);

	public Graph getGraph();
}
