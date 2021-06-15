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
import java.util.HashSet;

import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.net.TravelSurveyZone;

public class FixedMultiplierPassthroughAggregateMatrix implements AggregatePAMatrix {
	
	private final AggregatePAMatrix base;
	private final float multip;

	public FixedMultiplierPassthroughAggregateMatrix(AggregatePAMatrix baseMatrix, float multiplier) {
		base = baseMatrix;
		multip = multiplier;
	}
	
	@Override
	public Collection<TravelSurveyZone> getZones() {
		return base.getZones();
	}

	@Override
	public void put(TravelSurveyZone producer, TravelSurveyZone attractor, Float demand) {
		throw new RuntimeException("Writing to a read-only matrix");
	}

	@Override
	public float getDemand(TravelSurveyZone producer, TravelSurveyZone attractor) {
		return multip*base.getDemand(producer, attractor);
	}

	@Override
	public DemandMap getDemandMap(TravelSurveyZone producer) {
		return new FixedMultiplierPassthroughDemandMap(base.getDemandMap(producer),multip);
	}

	@Override
	public Collection<TravelSurveyZone> getProducers() {
		return multip <= 0? new HashSet<TravelSurveyZone>() : base.getProducers();
	}

}
