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

import java.util.Set;

/**An aggregator that combines multiple TravelSurveyZones into a region for balancing
 * 
 * Some trip purposes may require that trips be balanced not just at a network-wide
 * level but at a regional level as well. To do this, regions are defined as a set
 * of TravelSurveyZones, and the total trips produced by these zones must match the 
 * total number of trips attracted to these zones.
 * 
 * @author William
 *
 */
public class RegionalAreaAnalysisZone {
	private final Set<TravelSurveyZone> tszs;
	private AreaClass type;
	
	public RegionalAreaAnalysisZone(Set<TravelSurveyZone> zones) {
		tszs = zones;
	}
	
	public Set<TravelSurveyZone> getTSZs(){
		return tszs;
	}
	
	public AreaClass getAreaType() {
		return type;
	}
}
