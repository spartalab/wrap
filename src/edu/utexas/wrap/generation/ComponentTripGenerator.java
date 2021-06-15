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
package edu.utexas.wrap.generation;

import java.util.Collection;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.containers.FixedSizeDemandMap;
import edu.utexas.wrap.net.Demographic;
import edu.utexas.wrap.net.TravelSurveyZone;

/**A TripGenerator which generates multiple DemandMap components
 * according to a given array of GenerationRates. The width of the
 * input Demographic values are expected to be of the same size as
 * the number of rates provided. For example, if five rates are
 * provided, the supplied Demographic must provide at least five
 * values for each zone. Each rate-demographic dot product is then
 * stored as a DemandMap which forms a component of this generator.
 * 
 * @author William
 *
 */
public class ComponentTripGenerator implements TripGenerator {

	private Collection<TravelSurveyZone> zones;
	private GenerationRate[] rates;
	private DemandMap[] components;
	
	public ComponentTripGenerator(Collection<TravelSurveyZone> zones, GenerationRate[] generationRates) {
		this.zones = zones;
		rates = generationRates;
		components = IntStream.range(0, rates.length).mapToObj(i -> new FixedSizeDemandMap(zones)).toArray(DemandMap[]::new);
	}
	
	public DemandMap generate(Demographic demographic){
		
		IntStream.range(0, rates.length).forEach(category -> {
			zones.stream().forEach(tsz -> components[category].put(tsz, (float) rates[category].getRate(tsz) * demographic.valueFor(tsz)[category]));
		});
		
		DemandMap ret = new FixedSizeDemandMap(zones);

		zones.stream()
		.forEach(tsz -> {
			ret.put(tsz, 
					(float) Stream.of(components).mapToDouble(component -> component.get(tsz)).sum());
		});
		
		return ret;
	}
	
	public DemandMap[] getComponents() {
		return components;
	}
	

}

