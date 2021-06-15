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

import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.net.TravelSurveyZone;

public class FixedMultiplierPassthroughDemandMap implements DemandMap {
	private DemandMap parent;
	private float multiplier;

	public FixedMultiplierPassthroughDemandMap(DemandMap demandMap, float percent) {
		parent = demandMap;
		this.multiplier = percent;
	}

	@Override
	public float get(TravelSurveyZone dest) {
		return multiplier*parent.get(dest);
	}

	@Override
	public Collection<TravelSurveyZone> getZones() {
		return parent.getZones();
	}

	@Override
	public Float put(TravelSurveyZone dest, Float demand) {
		return parent.put(dest, demand/multiplier);
	}

	@Override
	public boolean isEmpty() {
		return parent.isEmpty();
	}

	@Override
	public Map<TravelSurveyZone, Double> doubleClone() {
		// TODO Auto-generated method stub
		throw new RuntimeException();
	}

}
