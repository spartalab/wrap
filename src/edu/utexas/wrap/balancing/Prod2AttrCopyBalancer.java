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

import java.util.Collection;

import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.net.TravelSurveyZone;

/** This class balances PAMaps by replacing all zones' productions
 * with the number of attractions from the same zone.
 * 
 * @author William
 *
 */
public class Prod2AttrCopyBalancer implements TripBalancer {

	@Override
	public PAMap balance(PAMap paMap) {
		Collection<TravelSurveyZone> nodes = paMap.getAttractors();
		nodes.addAll(paMap.getProducers());
		nodes.stream().forEach(n -> paMap.putProductions(n, paMap.getAttractions(n)));
		return paMap;
	}

}
