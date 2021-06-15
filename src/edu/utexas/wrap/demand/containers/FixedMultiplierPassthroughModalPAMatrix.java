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
import java.util.Collections;

import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.ModalPAMatrix;
import edu.utexas.wrap.demand.PAMatrix;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.TravelSurveyZone;

public class FixedMultiplierPassthroughModalPAMatrix implements ModalPAMatrix {
	private final float multiplier;
	private final Mode mode;
	private final PAMatrix aggregate;
	
	public FixedMultiplierPassthroughModalPAMatrix(Mode m, double pct, PAMatrix agg) {
		multiplier = (float) pct;
		mode = m;
		aggregate = agg;
	}
	
	public FixedMultiplierPassthroughModalPAMatrix(double pct, ModalPAMatrix modalMatrix) {
		multiplier = (float) pct;
		mode = modalMatrix.getMode();
		aggregate = modalMatrix;
	}
	
	@Override
	public Collection<TravelSurveyZone> getZones() {
		return aggregate.getZones();
	}

	@Override
	public void put(TravelSurveyZone producer, TravelSurveyZone attractor, Float demand) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Writing to a read-only matrix");
	}

	@Override
	public float getDemand(TravelSurveyZone producer, TravelSurveyZone attractor) {
		return multiplier*aggregate.getDemand(producer, attractor);
	}

	@Override
	public DemandMap getDemandMap(TravelSurveyZone producer) {
		return new FixedMultiplierPassthroughDemandMap(aggregate.getDemandMap(producer),multiplier);
	}

	@Override
	public Collection<TravelSurveyZone> getProducers() {
		return multiplier <= 0? Collections.<TravelSurveyZone>emptySet() : aggregate.getProducers();
	}

	@Override
	public Mode getMode() {
		return mode;
	}

}
