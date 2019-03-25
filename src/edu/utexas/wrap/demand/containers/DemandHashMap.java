package edu.utexas.wrap.demand.containers;

import java.util.Collection;
import java.util.HashMap;

import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;

public class DemandHashMap extends HashMap<Node, Float> implements DemandMap {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8268461681839852205L;
	Graph g;

	public DemandHashMap(Graph g) {
		super();
		this.g = g;
	}
	
	protected DemandHashMap(Graph g, DemandMap d) {
		super((DemandHashMap) d);
		this.g = g;
	}

	@Override
	public Float get(Node dest) {
		return this.getOrDefault(dest, 0.0F);
	}

	@Override
	public Graph getGraph() {
		return g;
	}

	@Override
	public Collection<Node> getNodes() {
		// TODO Auto-generated method stub
		return keySet();
	}

	@Override
	public Float getOrDefault(Node node, float f) {
		// TODO Auto-generated method stub
		return super.getOrDefault(node, f);
	}
}
