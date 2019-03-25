package edu.utexas.wrap.demand;

import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;

import java.util.HashMap;

/**A map from an origin and destination zone to the
 * number of trips between them.
 * 
 * Any PAMatrix should have the ability to retrieve
 * metadata which may be used in a trip-interchange
 * mode choice model.
 * 
 * @author William
 *
 */
public interface PAMatrix {

	public void putDemand(Node i, DemandMap d) ;

	public Object getAttribute(String type);
	
	public float getVOT();

	public Graph getGraph();
}
