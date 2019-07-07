package edu.utexas.wrap.demand.containers;

import java.util.Collection;
import java.util.Map;

import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;

public class FixedMultiplierPassthroughDemandMap implements DemandMap {
	private DemandMap parent;
	private float multiplier;

	public FixedMultiplierPassthroughDemandMap(DemandMap demandMap, float percent) {
		parent = demandMap;
		this.multiplier = percent;
	}

	@Override
	public Float get(Node dest) {
		return multiplier*parent.get(dest);
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
		return d == null? f : d*multiplier;
	}

	@Override
	public Float put(Node dest, Float demand) {
		return parent.put(dest, demand/multiplier);
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
