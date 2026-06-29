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
package edu.utexas.wrap.modechoice;

import edu.utexas.wrap.demand.ModalPAMap;
import edu.utexas.wrap.demand.PAMap;

/**An abstract class for performing mode choice at the trip-end level,
 * i.e., before trip distribution. Trip-end mode choice models determine
 * mode shares based only on the productions and attractions at each zone,
 * without considering the specific origin-destination pair. This approach
 * is simpler but less behaviorally realistic than trip-interchange models.
 * 
 * @author Will Alexander
 * @see TripInterchangeSplitter
 */
public abstract class TripEndSplitter {
	
	public abstract ModalPAMap split(PAMap map);
}
