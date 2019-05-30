package edu.utexas.wrap.assignment.bush;

import java.util.Map;
import java.util.Set;

import edu.utexas.wrap.demand.AutoDemandMap;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;
import it.unimi.dsi.fastutil.floats.Float2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**Child threads created by {@link edu.utexas.wrap.assignment.BushOriginFactory}
 * to build the initial bush for each origin
 * @author William
 *
 */
public class BushOriginBuilder extends Thread {
	Map<Mode, Map<Float, AutoDemandMap>> map;
	Node o;
	Graph g;
	public BushOrigin orig;
	Set<BushOrigin> origins;

	/**Default constructor
	 * @param g the graph on which the origin should build its bushes
	 * @param o the origin node
	 * @param origins 
	 */
	public BushOriginBuilder(Graph g, Node o, Set<BushOrigin> origins) {
		this.o = o;
		this.map = new Object2ObjectOpenHashMap<Mode, Map<Float, AutoDemandMap>>();
		this.g = g;
		this.origins = origins;
	}
 
	/**Add a full DemandMap to the set of bushes to be built
	 * @param m
	 */
	public void addMap(AutoDemandMap m) {
		map.putIfAbsent(m.getMode(), new Float2ObjectOpenHashMap<AutoDemandMap>());
		map.get(m.getMode()).put(m.getVOT(), m);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		orig = new BushOrigin(o);
		for (Mode c : map.keySet()) {
			for (Float vot : map.get(c).keySet()) {
				AutoDemandMap odm = map.get(c).get(vot);
				if (!odm.isEmpty()) orig.buildBush(g, vot, odm, c);
			}
		}
		orig.deleteInitMap();
		origins.add(orig);
	}
}
