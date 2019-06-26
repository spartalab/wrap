package edu.utexas.wrap.demand.containers;

import java.util.Collection;
import java.util.Map;

import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMaps;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;

public class DemandHashMap implements DemandMap {

	Graph g;
	private Map<Node,Float> map; 

	public DemandHashMap(Graph g) {
		this.g = g;
		map = Object2FloatMaps.synchronize(new Object2FloatOpenHashMap<Node>(g.numZones()),1.0f);
	}
	
	protected DemandHashMap(DemandHashMap d) {
		this.g = d.getGraph();
		map = Object2FloatMaps.synchronize(new Object2FloatOpenHashMap<Node>(d.map));
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
		return map.keySet();
	}

	/* (non-Javadoc)
	 * @see edu.utexas.wrap.demand.DemandMap#getOrDefault(edu.utexas.wrap.net.Node, float)
	 */
	@Override
	public Float getOrDefault(Node node, float f) {
		return map.getOrDefault(node, f);
	}

	@Override
	public Map<Node, Double> doubleClone() {
		Map<Node,Double> ret = new Object2DoubleOpenHashMap<Node>(map.size());
		for (Node key : map.keySet()) ret.put(key, get(key).doubleValue());
		return ret;
	}

	@Override
	public Float put(Node dest, Float demand) {
		return map.put(dest, demand);
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	
}
