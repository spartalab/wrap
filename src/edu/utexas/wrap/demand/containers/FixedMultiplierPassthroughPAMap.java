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

import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.net.TravelSurveyZone;

public class FixedMultiplierPassthroughPAMap implements PAMap {
	private PAMap base;
	private double rate;

	public FixedMultiplierPassthroughPAMap(PAMap baseMap, double rate) {
		base = baseMap;
		this.rate = rate;
	}

	@Override
	public Collection<TravelSurveyZone> getProducers() {
		return base.getProducers();
	}

	@Override
	public Collection<TravelSurveyZone> getAttractors() {
		return base.getAttractors();
	}

	@Override
	public float getAttractions(TravelSurveyZone z) {
		return (float) (rate*base.getAttractions(z));
	}

	@Override
	public float getProductions(TravelSurveyZone z) {
		return (float) rate*base.getProductions(z);
	}

	@Override
	public void putAttractions(TravelSurveyZone z, Float amt) {
		throw new RuntimeException("Writing to read-only matrix");
	}

	@Override
	public void putProductions(TravelSurveyZone z, Float amt) {
		throw new RuntimeException("Writing to read-only matrix");
	}

	@Override
	public DemandMap getProductionMap() {
		// TODO Auto-generated method stub
		throw new RuntimeException();
	}

	@Override
	public DemandMap getAttractionMap() {
		// TODO Auto-generated method stub
		throw new RuntimeException();
	}

}
