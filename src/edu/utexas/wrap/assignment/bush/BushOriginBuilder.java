package edu.utexas.wrap.assignment.bush;

import java.util.HashMap;
import java.util.Map;

import edu.utexas.wrap.VehicleClass;
import edu.utexas.wrap.demand.AutomotiveDemandMap;
import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;

public class BushOriginBuilder extends Thread {
	Map<VehicleClass, Map<Float, DemandMap>> map;
	Node o;
	Graph g;
	public BushOrigin orig;

	public BushOriginBuilder(Graph g, Node o) {
		this.o = o;
		this.map = new HashMap<VehicleClass, Map<Float, DemandMap>>();
		this.g = g;
	}
 
	public void addMap(AutomotiveDemandMap m) {
		map.putIfAbsent(m.getVehicleClass(), new HashMap<Float, DemandMap>());
		map.get(m.getVehicleClass()).put(m.getVOT(), m);
	}
	
	public void run() {
		orig = new BushOrigin(o);
		for (VehicleClass c : map.keySet()) {
			for (Float vot : map.get(c).keySet()) {
				Map<Node, Float> odm = map.get(c).get(vot);
				if (!odm.isEmpty()) orig.buildBush(g, vot, odm, c);
			}
		}
		orig.deleteInitMap();
	}
}
