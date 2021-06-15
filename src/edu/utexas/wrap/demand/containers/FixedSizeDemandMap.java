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
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.net.TravelSurveyZone;

public class FixedSizeDemandMap implements DemandMap {
//	private final Graph graph;
	private final Collection<TravelSurveyZone> zones;
	private final float[] demand;
	
	public FixedSizeDemandMap(Collection<TravelSurveyZone> zones) {
		this.zones = zones;
		demand = new float[zones.size()];
	}
	
	public FixedSizeDemandMap(DemandMap base) {
		zones = base.getZones();
		demand = new float[zones.size()];
		base.getZones().parallelStream().forEach(tsz -> demand[tsz.getOrder()] = base.get(tsz));
	}
	
	public FixedSizeDemandMap(Stream<DemandMap> baseMapStream) {
		Collection<DemandMap> baseMaps = baseMapStream.collect(Collectors.toSet());
		zones = baseMaps.stream().findFirst().get().getZones();
		demand = new float[zones.size()];
		zones.forEach(tsz -> demand[tsz.getOrder()] = (float)
				baseMaps.parallelStream()
				.mapToDouble(dm -> (double) dm.get(tsz))
				.sum()
			);
		
	}

	public float get(TravelSurveyZone dest) {
		try{
			return demand[dest.getOrder()];
		} catch (NullPointerException e) {
			return 0.0f;
		}
	}

	public Collection<TravelSurveyZone> getZones() {
		return zones;
	}

	public Float put(TravelSurveyZone dest, Float put) {
		int idx = dest.getOrder();
		Float d = demand[idx];
		demand[idx] = put;
		return d;
	}

	public boolean isEmpty() {
		for (Float d : demand) if (d > 0) return false;
		return true;
	}

	public Map<TravelSurveyZone, Double> doubleClone() {
		Map<TravelSurveyZone, Double> ret = new HashMap<TravelSurveyZone, Double>();
		for (TravelSurveyZone n : zones) {
			ret.put(n,(double) demand[n.getOrder()]);
		}
		return ret;
	}

}
