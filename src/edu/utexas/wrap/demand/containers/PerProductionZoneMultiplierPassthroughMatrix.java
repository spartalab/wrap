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
import java.util.stream.Collectors;

import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.net.TravelSurveyZone;

public class PerProductionZoneMultiplierPassthroughMatrix implements AggregatePAMatrix {

	AggregatePAMatrix parent;
	Map<TravelSurveyZone,Float> rates;
	
	public PerProductionZoneMultiplierPassthroughMatrix(AggregatePAMatrix initialMatrix, Map<TravelSurveyZone, Float> perZoneRates) {
		parent = initialMatrix;
		rates = perZoneRates;
	}

	@Override
	public void put(TravelSurveyZone producer, TravelSurveyZone attractor, Float demand) {
		// TODO Auto-generated method stub
		throw new RuntimeException();
	}

	@Override
	public float getDemand(TravelSurveyZone producer, TravelSurveyZone attractor) {
		return (float) (rates.get(producer)*parent.getDemand(producer,attractor));
	}

	@Override
	public DemandMap getDemandMap(TravelSurveyZone producer) {
		return new FixedMultiplierPassthroughDemandMap(parent.getDemandMap(producer),rates.get(producer).floatValue());
	}

	@Override
	public Collection<TravelSurveyZone> getProducers() {
		return parent.getProducers().parallelStream().filter(zone -> rates.get(zone)>0).collect(Collectors.toSet());
	}

}
