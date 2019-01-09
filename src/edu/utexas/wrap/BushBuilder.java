package edu.utexas.wrap;

import java.util.Map;

class BushBuilder extends Thread {
	Map<VehicleClass, Map<Float, Map<Node, Float>>> map;
	Node o;
	Graph g;
	Origin orig;

	public BushBuilder(Graph g, Node o, Map<VehicleClass, Map<Float, Map<Node, Float>>> map) {
		this.o = o;
		this.map = map;
		this.g = g;
	}

	public void run() {
		orig = new Origin(o);
		for (VehicleClass c : map.keySet()) {
			for (Float vot : map.get(c).keySet()) {
				Map<Node, Float> odm = map.get(c).get(vot);
				if (!odm.isEmpty()) orig.buildBush(g, vot, odm, c);
			}
		}
		orig.deleteInitMap();
	}
}
