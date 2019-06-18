package edu.utexas.wrap.demand.containers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;

public class FixedSizeDemandMap implements DemandMap {
	private final Graph graph;
	private float[] demand;
	
	public FixedSizeDemandMap(Graph g) {
		graph = g;
		demand = new float[g.numNodes()];
	}

	@Override
	public Float get(Node dest) {
		// TODO Auto-generated method stub
		return demand[graph.getOrder(dest)];
	}

	@Override
	public Graph getGraph() {
		// TODO Auto-generated method stub
		return graph;
	}

	@Override
	public Collection<Node> getNodes() {
		// TODO Auto-generated method stub
		return graph.getNodes();
	}

	@Override
	public Float getOrDefault(Node node, float f) {
		// TODO Auto-generated method stub
		return demand[graph.getOrder(node)];
	}

	@Override
	public Float put(Node dest, Float put) {
		// TODO Auto-generated method stub
		Float d = demand[graph.getOrder(dest)];
		demand[graph.getOrder(dest)] = put;
		return d;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		for (Float d : demand) if (d > 0) return false;
		return true;
	}

	@Override
	public Map<Node, Double> doubleClone() {
		// TODO Auto-generated method stub
		Map<Node, Double> ret = new HashMap<Node, Double>();
		for (Node n : graph.getNodes()) {
			ret.put(n,(double) demand[graph.getOrder(n)]);
		}
		return ret;
	}

}
