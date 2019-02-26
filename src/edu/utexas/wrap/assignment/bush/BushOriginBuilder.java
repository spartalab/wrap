package edu.utexas.wrap.assignment.bush;

import java.util.HashMap;
import java.util.Map;

import edu.utexas.wrap.demand.AutomotiveDemandMap;
import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;

/**Child threads created by {@link edu.utexas.wrap.assignment.BushLoader}
 * to build the initial bush for each origin
 * @author William
 *
 */
public class BushOriginBuilder extends Thread {
	Map<Mode, Map<Float, DemandMap>> map;
	Node o;
	Graph g;
	public BushOrigin orig;

	public BushOriginBuilder(Graph g, Node o) {
		this.o = o;
		this.map = new HashMap<Mode, Map<Float, DemandMap>>();
		this.g = g;
	}
 
	public void addMap(AutomotiveDemandMap m) {
		map.putIfAbsent(m.getMode(), new HashMap<Float, DemandMap>());
		map.get(m.getMode()).put(m.getVOT(), m);
	}
	
	public void run() {
		orig = new BushOrigin(o);
		for (Mode c : map.keySet()) {
			for (Float vot : map.get(c).keySet()) {
				DemandMap odm = map.get(c).get(vot);
				if (!odm.isEmpty()) orig.buildBush(g, vot, odm, c);
			}
		}
		orig.deleteInitMap();
	}
}
