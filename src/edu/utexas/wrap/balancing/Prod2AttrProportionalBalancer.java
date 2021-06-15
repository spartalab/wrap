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

import java.util.Set;

import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.net.RegionalAreaAnalysisZone;

/**This class balances PAMaps by calculating the total productions and
 * attractions, dividing the latter by the former, and multiplying each
 * TSZ's productions by that ratio. In this way, the total productions
 * are scaled up to match the total productions. 
 * 
 * If RegionalAreaAnalysisZones are provided, this is balanced on a
 * per-RAA basis; otherwise, the balancing is performed on a network-
 * wide basis
 * 
 * @author William
 *
 */
public class Prod2AttrProportionalBalancer implements TripBalancer {

	private Set<RegionalAreaAnalysisZone> raas;
	
	public Prod2AttrProportionalBalancer(Set<RegionalAreaAnalysisZone> regionalAnalysisZones) {
		raas = regionalAnalysisZones;
	}
	
	@Override
	public PAMap balance(PAMap paMap) {
		if (raas != null) raas.stream().forEach(raa -> {
			float prods = (float) raa.getTSZs().stream().mapToDouble(tsz ->paMap.getProductions(tsz)).sum();
			float attrs = (float) raa.getTSZs().stream().mapToDouble(tsz -> paMap.getAttractions(tsz)).sum();

			float prop = attrs/prods;
			raa.getTSZs().stream().forEach(tsz -> paMap.putProductions(tsz, paMap.getProductions(tsz)*prop));
		});
		else {
			double prods = paMap.getProducers().stream().mapToDouble(prod -> paMap.getProductions(prod)).sum();
			double attrs = paMap.getAttractors().stream().mapToDouble(attr -> paMap.getAttractions(attr)).sum();
			
			double scale = attrs/prods;
			paMap.getProducers().stream().forEach(prod -> paMap.putProductions(prod, (float) (paMap.getProductions(prod)*scale)));
		}
		return paMap;
	}

}
