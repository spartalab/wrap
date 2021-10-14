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

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.utexas.wrap.TimePeriod;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.demand.ODProfile;
import edu.utexas.wrap.marketsegmentation.Purpose;
import edu.utexas.wrap.modechoice.Mode;

public class SegmentedODProfile implements ODProfile {
	private final Map<TimePeriod,ODMatrix> matrices;
	private final Map<TimePeriod,Float> vots;
	private final Mode mode;
	private final Purpose parent;

	public SegmentedODProfile(
			ODMatrix dailyDepartures,
			Map<TimePeriod,Float> departureRates,
			ODMatrix dailyArrivals,
			Map<TimePeriod,Float> arrivalRates,
			Map<TimePeriod,Float> vots,
			Purpose parent
			) {
		mode = dailyDepartures.getMode();
		this.vots = vots;
		this.parent = parent;
		matrices = Stream.of(TimePeriod.values())
				.filter(tp -> departureRates.containsKey(tp) || arrivalRates.containsKey(tp))
				.collect(
						Collectors.toMap(
								Function.identity(),
								tp -> {
									ODMatrix 
									deps = new FixedMultiplierPassthroughODMatrix(
											dailyDepartures,
											departureRates.getOrDefault(tp,0.0f)
											),
									arrs = new FixedMultiplierPassthroughODMatrix(
											dailyArrivals,
											arrivalRates.getOrDefault(tp,0.0f)
											),
									combined = new AddingPassthroughODMatrix(
											deps,arrs
											);
									return combined;
								}
								)
						);
	}
	
	public SegmentedODProfile(Map<TimePeriod,ODMatrix> matrices, Map<TimePeriod,Float> vots, Mode mode, Purpose parent) {
		this.matrices = matrices;
		this.vots = vots;
		this.mode = mode;
		this.parent = parent;
	}

	@Override
	public ODMatrix getMatrix(TimePeriod period) {
		return matrices.get(period);
	}
	
	@Override
	public Float getVOT(TimePeriod period) {
		return vots.getOrDefault(period,0.0f);
	}

	@Override
	public Mode getMode() {
		return mode;
	}
	
	@Override
	public Purpose getTripPurpose() {
		return parent;
	}
}
