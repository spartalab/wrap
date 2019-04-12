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
	
	protected DemandHashMap(DemandHashMap d) {
		super(d);
		this.g = d.getGraph();
	}
	
	/* (non-Javadoc)
	 * @see java.util.HashMap#clone()
	 */
	@Override
	public DemandHashMap clone() {
		return new DemandHashMap(this);
	}

	/* (non-Javadoc)
	 * @see edu.utexas.wrap.demand.DemandMap#get(edu.utexas.wrap.net.Node)
	 */
	@Override
	public Float get(Node dest) {
		return this.getOrDefault(dest, 0.0F);
	}

	/* (non-Javadoc)
	 * @see edu.utexas.wrap.demand.DemandMap#getGraph()
	 */
	@Override
	public Graph getGraph() {
		return g;
	}

	/* (non-Javadoc)
	 * @see edu.utexas.wrap.demand.DemandMap#getNodes()
	 */
	@Override
	public Collection<Node> getNodes() {
		return keySet();
	}

	/* (non-Javadoc)
	 * @see edu.utexas.wrap.demand.DemandMap#getOrDefault(edu.utexas.wrap.net.Node, float)
	 */
	@Override
	public Float getOrDefault(Node node, float f) {
		return super.getOrDefault(node, f);
	}

	
}
