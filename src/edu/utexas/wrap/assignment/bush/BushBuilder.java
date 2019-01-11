package edu.utexas.wrap.assignment.bush;

import java.util.Map;

import edu.utexas.wrap.VehicleClass;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;

public class BushBuilder extends Thread {
	Map<VehicleClass, Map<Float, Map<Node, Float>>> map;
	Node o;
	Graph g;
	public BushOrigin orig;

	public BushBuilder(Graph g, Node o, Map<VehicleClass, Map<Float, Map<Node, Float>>> map) {
		this.o = o;
		this.map = map;
		this.g = g;
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
