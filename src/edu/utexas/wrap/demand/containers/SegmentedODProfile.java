package edu.utexas.wrap.demand.containers;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.utexas.wrap.TimePeriod;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.demand.ODProfile;

public class SegmentedODProfile implements ODProfile {
	Map<TimePeriod,ODMatrix> matrices;

	public SegmentedODProfile(
			ODMatrix dailyDepartures,
			Map<TimePeriod,Float> departureRates,
			ODMatrix dailyArrivals,
			Map<TimePeriod,Float> arrivalRates
			) {
		matrices = Stream.of(TimePeriod.values())
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
												departureRates.get(tp)
												),
										new FixedSizeODMatrix<FixedSizeDemandMap>(
												dailyArrivals.getVOT(),
												dailyArrivals.getMode(),
												dailyArrivals,
												arrivalRates.get(tp)
												)
										)
								)
						);
	}

	@Override
	public ODMatrix getMatrix(TimePeriod period) {
		return matrices.get(period);
	}

}
