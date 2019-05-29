package edu.utexas.wrap.assignment.bush;

import java.io.IOException;
import java.util.Set;

import edu.utexas.wrap.demand.AutoDemandMap;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;

public class BushOriginLoader extends BushOriginBuilder {

	public BushOriginLoader(Graph g, Node o, Set<BushOrigin> origins) {
		super(g, o, origins);
	}

	@Override
	public void run() {
		orig = new BushOrigin(o);
		for (Mode c : map.keySet()) {
			for (Float vot : map.get(c).keySet()) {
				AutoDemandMap odm = map.get(c).get(vot);
				if (!odm.isEmpty()) try {
					orig.loadBush(g, vot, odm, c);
				} catch (IOException e) {
					orig.buildBush(g, vot, odm, c);
				}
			}
		}
		orig.deleteInitMap();
		origins.add(orig);
	}
}
