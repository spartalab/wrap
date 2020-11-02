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
