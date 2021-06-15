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
package edu.utexas.wrap.demand.containers;

import java.util.Collection;

import edu.utexas.wrap.TimePeriod;
import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.TravelSurveyZone;

public class AddingODMatrix implements ODMatrix {

	private final DemandMap[] demandMaps;
	private final TimePeriod tp;
	private final Mode mode;
	private final Collection<TravelSurveyZone> zones;
	
	public AddingODMatrix(Collection<ODMatrix> ods, Mode mode, TimePeriod tp, Collection<TravelSurveyZone> zones) {
		this.mode = mode;
		this.tp = tp;
		this.zones = zones;
		
		demandMaps = new DemandMap[zones.size()];
		zones.parallelStream().forEach(origin ->{
			DemandMap dm = new FixedSizeDemandMap(zones);
			zones.stream().forEach(destination -> dm.put(destination, (float) ods.stream().mapToDouble(od -> od.getDemand(origin, destination)).sum()));
			demandMaps[origin.getOrder()] = dm;
		});
	}
	
	@Override
	public Mode getMode() {
		return mode;
	}

	@Override
	public float getDemand(TravelSurveyZone origin, TravelSurveyZone destination) {
		return demandMaps[origin.getOrder()].get(destination);
	}

	@Override
	public void put(TravelSurveyZone origin, TravelSurveyZone destination, Float demand) {
		throw new RuntimeException("Read-only matrix");
	}


	@Override
	public TimePeriod timePeriod() {
		return tp;
	}

	@Override
	public Collection<TravelSurveyZone> getZones() {
		return zones;
	}

	@Override
	public DemandMap getDemandMap(TravelSurveyZone origin) {
		// TODO Auto-generated method stub
		return demandMaps[origin.getOrder()];
	}

}
