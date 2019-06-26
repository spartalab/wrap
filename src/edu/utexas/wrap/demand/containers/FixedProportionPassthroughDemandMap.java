package edu.utexas.wrap.demand.containers;

import java.util.Collection;
import java.util.Map;

import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;

public class FixedProportionPassthroughDemandMap implements DemandMap {
	private DemandMap parent;
	private float percent;

	public FixedProportionPassthroughDemandMap(DemandMap demandMap, float percent) {
		parent = demandMap;
		this.percent = percent;
	}

	@Override
	public Float get(Node dest) {
		return percent*parent.get(dest);
	}

	@Override
	public Graph getGraph() {
		return parent.getGraph();
	}

	@Override
	public Collection<Node> getNodes() {
		return parent.getNodes();
	}

	@Override
	public Float getOrDefault(Node node, float f) {
		Float d = parent.get(node);
		return d == null? f : d*percent;
	}

	@Override
	public Float put(Node dest, Float demand) {
		return parent.put(dest, demand/percent);
	}

	@Override
	public boolean isEmpty() {
		return parent.isEmpty();
	}

	@Override
	public Map<Node, Double> doubleClone() {
		// TODO Auto-generated method stub
		throw new RuntimeException();
	}

}
