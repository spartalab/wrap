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
package edu.utexas.wrap.balancing;

import edu.utexas.wrap.demand.PAMap;

/**An interface which takes a PAMap of arbitrary number of productions and attractions,
 * then returns a modified PAMap which guarantees that the number of productions is equal
 * to the number of attractions.
 * 
 * @author William
 *
 */
public interface TripBalancer {

	/**
	 * Balance Trips such that the total number of productions is equivalent to the total number of attractions
	 */
	public PAMap balance(PAMap paMap); //TODO don't balance in-place
}
