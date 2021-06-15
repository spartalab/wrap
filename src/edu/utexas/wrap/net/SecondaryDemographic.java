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
package edu.utexas.wrap.net;

import java.util.stream.IntStream;

import edu.utexas.wrap.demand.DemandMap;

/**A demographic whose components are a series of DemandMaps
 * 
 * For trips which depend on the amount of other trips taken,
 * this class provides a mechanism for using the number of trips
 * from a given purpose (can be unbalanced or balanced) as the 
 * base demographic for another trip purpose's generation step
 * 
 * @author William
 *
 */
public class SecondaryDemographic implements Demographic {
	
	private final DemandMap[] components;
	
	public SecondaryDemographic(DemandMap[] components) {
		this.components = components;
	}

	@Override
	public Float[] valueFor(TravelSurveyZone zone) {
		return IntStream.range(0, components.length)
				.mapToObj(index -> (float) components[index].get(zone))
				.toArray(Float[]::new);
	}
}
