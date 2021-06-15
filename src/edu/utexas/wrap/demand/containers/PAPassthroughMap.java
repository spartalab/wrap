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
import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.net.TravelSurveyZone;

public class PAPassthroughMap implements PAMap {
	private DemandMap prods, attrs;
	
	public PAPassthroughMap(DemandMap productions, DemandMap attractions) {
		prods = productions;
		attrs = attractions;
	}

	@Override
	public Collection<TravelSurveyZone> getProducers() {
		return prods == null? Collections.emptySet() : prods.getZones();
	}

	@Override
	public Collection<TravelSurveyZone> getAttractors() {
		return attrs == null? Collections.emptySet() : attrs.getZones();
	}

	@Override
	public float getAttractions(TravelSurveyZone z) {
		return attrs == null? 0.0f : attrs.get(z);
	}

	@Override
	public float getProductions(TravelSurveyZone z) {
		return prods == null? 0.0f : prods.get(z);
	}

	@Override
	public void putAttractions(TravelSurveyZone z, Float amt) {
		attrs.put(z, amt);
	}

	@Override
	public void putProductions(TravelSurveyZone z, Float amt) {
		prods.put(z, amt);
	}

	@Override
	public DemandMap getProductionMap() {
		return prods;
	}

	@Override
	public DemandMap getAttractionMap() {
		return attrs;
	}

}
