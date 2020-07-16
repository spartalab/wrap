package edu.utexas.wrap.demand.containers;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;

public class DemandHashMap implements DemandMap {

	Graph g;
	private final Collection<TravelSurveyZone> zones;
	private Map<TravelSurveyZone,Float> map; 

	public DemandHashMap(Collection<TravelSurveyZone> zones) {
		this.zones = zones;
		map = new ConcurrentHashMap<TravelSurveyZone,Float>(zones.size(),1.0f);
	}
	
	protected DemandHashMap(DemandHashMap d) {
		this.zones = d.getZones();
		map = new ConcurrentHashMap<TravelSurveyZone,Float>(d.map);
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
	public float get(TravelSurveyZone dest) {
		return map.getOrDefault(dest, 0.0f);
	}


	/* (non-Javadoc)
	 * @see edu.utexas.wrap.demand.DemandMap#getNodes()
	 */
	@Override
	public Collection<TravelSurveyZone> getZones() {
		return zones;
	}

	@Override
	public Map<TravelSurveyZone, Double> doubleClone() {
		Map<TravelSurveyZone, Double> ret = new ConcurrentHashMap<TravelSurveyZone,Double>(map.size());
		for (TravelSurveyZone key : map.keySet()) ret.put(key, (double) get(key));
		return ret;
	}

	@Override
	public Float put(TravelSurveyZone attr, Float demand) {
		return map.put(attr, demand);
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	
}
