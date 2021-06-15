/*
    wrap - free and open-source urban transportation modeling software
    Copyright (C) 2017 the wrap project, The University of Texas at Austin

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
	public DemandHashMap clone() {
		return new DemandHashMap(this);
	}

	/* (non-Javadoc)
	 * @see edu.utexas.wrap.demand.DemandMap#get(edu.utexas.wrap.net.Node)
	 */
	public float get(TravelSurveyZone dest) {
		return map.getOrDefault(dest, 0.0f);
	}


	/* (non-Javadoc)
	 * @see edu.utexas.wrap.demand.DemandMap#getNodes()
	 */
	public Collection<TravelSurveyZone> getZones() {
		return zones;
	}

	public Map<TravelSurveyZone, Double> doubleClone() {
		Map<TravelSurveyZone, Double> ret = new ConcurrentHashMap<TravelSurveyZone,Double>(map.size());
		for (TravelSurveyZone key : map.keySet()) ret.put(key, (double) get(key));
		return ret;
	}

	public Float put(TravelSurveyZone attr, Float demand) {
		return map.put(attr, demand);
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	
}
