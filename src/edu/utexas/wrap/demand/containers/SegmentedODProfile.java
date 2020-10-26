package edu.utexas.wrap.demand.containers;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.utexas.wrap.TimePeriod;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.demand.ODProfile;
import edu.utexas.wrap.modechoice.Mode;

public class SegmentedODProfile implements ODProfile {
	private final Map<TimePeriod,ODMatrix> matrices;
	private final Mode mode;

	public SegmentedODProfile(
			ODMatrix dailyDepartures,
			Map<TimePeriod,Float> departureRates,
			ODMatrix dailyArrivals,
			Map<TimePeriod,Float> arrivalRates
			) {
		mode = dailyDepartures.getMode();
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
	
	public SegmentedODProfile(Map<TimePeriod,ODMatrix> matrices, Mode mode) {
		this.matrices = matrices;
		this.mode = mode;
	}

	@Override
	public ODMatrix getMatrix(TimePeriod period) {
		return matrices.get(period);
	}

	@Override
	public Mode getMode() {
		return mode;
	}
}
