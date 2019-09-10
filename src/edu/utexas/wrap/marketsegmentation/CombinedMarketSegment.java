package edu.utexas.wrap.marketsegmentation;

import java.util.Collection;
import java.util.function.ToDoubleFunction;

import edu.utexas.wrap.net.TravelSurveyZone;

public class CombinedMarketSegment<T extends MarketSegment> implements MarketSegment {

	private Collection<T> segments;
	
	@Override
	public ToDoubleFunction<TravelSurveyZone> attributeDataGetter() {
		return tsz -> segments.parallelStream().mapToDouble(seg -> seg.attributeDataGetter().applyAsDouble(tsz)).sum();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder("Combined segment: ");
		segments.parallelStream().forEach(seg -> sb.append(seg.toString()+","));
		return sb.toString();
	}
}
