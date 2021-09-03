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

/**Interface for defining a cost of travel between two zones
 * 
 * This interface provides a mechanism for defining a matrix of costs
 * between any two TravelSurveyZones in a network. That is, given any
 * two TSZs, each implementation of this interface provides some arbitrary
 * cost of travel between the two TSZs.
 * 
 * @author William
 *
 */
public interface NetworkSkim {

	public float getCost(TravelSurveyZone producer, TravelSurveyZone attractor);
	
}
