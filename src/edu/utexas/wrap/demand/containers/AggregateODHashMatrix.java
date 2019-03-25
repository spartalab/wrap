package edu.utexas.wrap.demand.containers;

import java.util.HashMap;

import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.net.Node;

/**Generic origin-destination matrix which does not specify the mode
 * to be used. This should be read in from a file or generated by a
 * trip distribution module.
 * @author William
 *
 */
public class AggregateODHashMatrix extends HashMap<Node, DemandHashMap> implements ODMatrix {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4760691392227828734L;

	@Override
	public Float getDemand(Node origin, Node destination) {
		// TODO Auto-generated method stub
		return get(origin) == null ? null : get(origin).getOrDefault(destination, 0.0F);
	}

	@Override
	public void put(Node origin, Node destination, Float demand) {
		// TODO Auto-generated method stub

	}

}
