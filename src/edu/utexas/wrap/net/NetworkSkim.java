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

/**Interface for defining a cost of travel between two zones.
 * 
 * A NetworkSkim is a zone-to-zone impedance matrix that stores the generalized
 * cost of travel (e.g., travel time, distance, or composite cost) between all
 * pairs of {@link TravelSurveyZone}s in the network. Skims are computed from
 * the current assigned network state and serve as inputs to trip distribution
 * and mode choice models in subsequent iterations of the four-step process.
 * 
 * <p>Skims are typically updated after each traffic assignment iteration to
 * reflect the latest congested travel times, enabling feedback between the
 * assignment and demand modeling steps.
 * 
 * @author Will Alexander
 * @see FixedSizeNetworkSkim
 */
public interface NetworkSkim {

	public float getCost(TravelSurveyZone producer, TravelSurveyZone attractor);
	
	public void putCost(TravelSurveyZone producer, TravelSurveyZone attractor, float cost);
	
}
