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
	private Map<TimePeriod,ODMatrix> matrices;
	private Mode mode;

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
								tp -> new FixedSizeODMatrix<FixedSizeDemandMap>(
										dailyDepartures.getVOT(),
										dailyDepartures.getMode(),
										new FixedSizeODMatrix<FixedSizeDemandMap>(
												dailyDepartures.getVOT(),
												dailyDepartures.getMode(),
												dailyDepartures,
												departureRates.getOrDefault(tp,0.0f)
												),
										new FixedSizeODMatrix<FixedSizeDemandMap>(
												dailyArrivals.getVOT(),
												dailyArrivals.getMode(),
												dailyArrivals,
												arrivalRates.getOrDefault(tp,0.0f)
												)
										)
								)
						);
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
