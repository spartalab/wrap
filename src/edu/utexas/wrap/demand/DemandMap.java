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
package edu.utexas.wrap.demand;

import java.util.Collection;
import java.util.Map;

import edu.utexas.wrap.net.TravelSurveyZone;

/**A simple map from a node to a demand level
 * @author William
 *
 */
public interface DemandMap {

	/**
	 * @param dest the Node whose demand level is measured
	 * @return the demand at the given Node
	 */
	public float get(TravelSurveyZone dest);

	/**
	 * @return the collection of nodes for which there is demand
	 */
	public Collection<TravelSurveyZone> getZones();
	
	/**
	 * @param dest the Node to whence there is demand
	 * @param demand the amount of demand present at the Node
	 * @return the previous mapping, if there was one present
	 */
	public Float put(TravelSurveyZone dest, Float demand);

	/**
	 * @return whether this Map has entries
	 */
	public boolean isEmpty();

	/**
	 * @return a copy of the DemandMap with all values as doubles
	 */
	public Map<TravelSurveyZone, Double> doubleClone();

	public default double totalDemand() {
		return getZones().stream().mapToDouble(this::get).sum();
	};
}
