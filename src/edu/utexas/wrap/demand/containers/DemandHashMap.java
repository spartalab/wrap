package edu.utexas.wrap.demand.containers;

import java.util.Collection;
import java.util.Map;

import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;
import it.unimi.dsi.fastutil.objects.Object2DoubleMaps;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;

public class DemandHashMap implements DemandMap {

	Graph g;
	private Map<TravelSurveyZone,Double> map; 

	public DemandHashMap(Graph g) {
		this.g = g;
		map = Object2DoubleMaps.synchronize(new Object2DoubleOpenHashMap<TravelSurveyZone>(g.numZones()),1.0f);
	}
	
	protected DemandHashMap(DemandHashMap d) {
		this.g = d.getGraph();
		map = Object2DoubleMaps.synchronize(new Object2DoubleOpenHashMap<TravelSurveyZone>(d.map));
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
	public Double get(TravelSurveyZone dest) {
		return this.getOrDefault(dest, 0.0);
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
	public Collection<TravelSurveyZone> getZones() {
		return map.keySet();
	}

	/* (non-Javadoc)
	 * @see edu.utexas.wrap.demand.DemandMap#getOrDefault(edu.utexas.wrap.net.Node, float)
	 */
	@Override
	public Double getOrDefault(TravelSurveyZone attrZone, Double f) {
		return map.getOrDefault(attrZone, f);
	}

	@Override
	public Map<TravelSurveyZone, Double> doubleClone() {
		Map<TravelSurveyZone,Double> ret = new Object2DoubleOpenHashMap<TravelSurveyZone>(map.size());
		for (TravelSurveyZone key : map.keySet()) ret.put(key, get(key).doubleValue());
		return ret;
	}

	@Override
	public Double put(TravelSurveyZone attr, Double demand) {
		return map.put(attr, demand);
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	
}
