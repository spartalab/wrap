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
package edu.utexas.wrap.util;

import java.util.Map;
import java.util.stream.Stream;

import edu.utexas.wrap.TimePeriod;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.demand.ODProfile;
import edu.utexas.wrap.demand.containers.SegmentedODProfile;
import edu.utexas.wrap.demand.containers.TransposeODMatrix;

public class TimeOfDaySplitter {
	final Map<TimePeriod,Float> depRates, arrRates,vots;

	public TimeOfDaySplitter(Map<TimePeriod, Float> departureRates, Map<TimePeriod, Float> arrivalRates,Map<TimePeriod,Float> vots) {
		depRates = departureRates;
		arrRates = arrivalRates;
		this.vots = vots;
	}

	public Stream<ODProfile> split(Stream<ODMatrix> dailyODs) {
		return dailyODs.map(daily -> new SegmentedODProfile(daily,depRates,new TransposeODMatrix(daily),arrRates, vots));
	}

}
