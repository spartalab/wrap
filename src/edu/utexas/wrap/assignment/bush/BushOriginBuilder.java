package edu.utexas.wrap.assignment.bush;

import java.util.HashMap;
import java.util.Map;

import edu.utexas.wrap.demand.containers.AutoDemandHashMap;
import edu.utexas.wrap.demand.containers.DemandHashMap;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;

/**Child threads created by {@link edu.utexas.wrap.assignment.BushLoader}
 * to build the initial bush for each origin
 * @author William
 *
 */
public class BushOriginBuilder extends Thread {
	Map<Mode, Map<Float, DemandHashMap>> map;
	Node o;
	Graph g;
	public BushOrigin orig;

	/**Default constructor
	 * @param g the graph on which the origin should build its bushes
	 * @param o the origin node
	 */
	public BushOriginBuilder(Graph g, Node o) {
		this.o = o;
		this.map = new HashMap<Mode, Map<Float, DemandHashMap>>();
		this.g = g;
	}
 
	/**Add a full DemandMap to the set of bushes to be built
	 * @param m
	 */
	public void addMap(AutoDemandHashMap m) {
		map.putIfAbsent(m.getMode(), new HashMap<Float, DemandHashMap>());
		map.get(m.getMode()).put(m.getVOT(), m);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		orig = new BushOrigin(o);
		for (Mode c : map.keySet()) {
			for (Float vot : map.get(c).keySet()) {
				DemandHashMap odm = map.get(c).get(vot);
				if (!odm.isEmpty()) orig.buildBush(g, vot, odm, c);
			}
		}
		orig.deleteInitMap();
	}
}
