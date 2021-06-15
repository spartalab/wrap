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
import java.util.stream.Stream;

import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.net.TravelSurveyZone;

public class FixedSizePAMap implements PAMap {
	final DemandMap prods, attrs;
	
	public FixedSizePAMap(PAMap base) {
		prods = new FixedSizeDemandMap(base.getProductionMap());
		attrs = new FixedSizeDemandMap(base.getAttractionMap());
	}
	
	public FixedSizePAMap(Stream<DemandMap> prodMapStream, Stream<DemandMap> attrMapStream) {
		prods = new FixedSizeDemandMap(prodMapStream);
		attrs = new FixedSizeDemandMap(attrMapStream);
	}

	public Collection<TravelSurveyZone> getProducers() {
		return prods.getZones();
	}

	public Collection<TravelSurveyZone> getAttractors() {
		return attrs.getZones();
	}

	public float getAttractions(TravelSurveyZone z) {
		return attrs.get(z);
	}

	public float getProductions(TravelSurveyZone z) {
		return prods.get(z);
	}

	public void putAttractions(TravelSurveyZone z, Float amt) {
		attrs.put(z, amt);
	}

	public void putProductions(TravelSurveyZone z, Float amt) {
		prods.put(z, amt);
	}

	public DemandMap getProductionMap() {
		return prods;
	}

	public DemandMap getAttractionMap() {
		return attrs;
	}

}
