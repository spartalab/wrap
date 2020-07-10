package edu.utexas.wrap.util;

import java.util.Map;
import java.util.stream.Stream;

import edu.utexas.wrap.TimePeriod;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.demand.ODProfile;

public class TimeOfDaySplitter {

	public TimeOfDaySplitter(Map<TimePeriod, Double> departureRates, Map<TimePeriod, Double> arrivalRates) {
		throw new RuntimeException("Not yet implemented");
	}

	public Stream<ODProfile> split(Stream<ODMatrix> dailyODs) {
		throw new RuntimeException("Not yet implemented");
	}

}
