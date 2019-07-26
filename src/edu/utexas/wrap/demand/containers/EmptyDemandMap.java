package edu.utexas.wrap.demand.containers;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.utexas.wrap.demand.AutoDemandMap;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;

public class EmptyDemandMap implements AutoDemandMap {
	private Graph g;
	public EmptyDemandMap(Graph gPrime) {
		// TODO Auto-generated constructor stub
		g = gPrime;
	}

	@Override
	public Float get(Node dest) {
		// TODO Auto-generated method stub
		return 0.0f;
	}

	@Override
	public Graph getGraph() {
		// TODO Auto-generated method stub
		return g;
	}

	@Override
	public Collection<Node> getNodes() {
		// TODO Auto-generated method stub
		return new HashSet<Node>(0,1.0f);
	}

	@Override
	public Float getOrDefault(Node node, float f) {
		// TODO Auto-generated method stub
		return 0.0f;
	}

	@Override
	public Float put(Node dest, Float demand) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Unable to add demand to an empty demand map");
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public Map<Node, Double> doubleClone() {
		// TODO Auto-generated method stub
		return new HashMap<Node,Double>();
	}

	@Override
	public Float getVOT() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Mode getMode() {
		// TODO Auto-generated method stub
		return null;
	}

}
