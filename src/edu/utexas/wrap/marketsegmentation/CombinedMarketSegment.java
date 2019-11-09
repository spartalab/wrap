package edu.utexas.wrap.marketsegmentation;

import java.util.Collection;
import java.util.function.ToDoubleFunction;

import edu.utexas.wrap.net.TravelSurveyZone;

public class CombinedMarketSegment<T extends MarketSegment> implements MarketSegment {

	private Collection<T> segments;
	private int hash;
	
	@Override
	public ToDoubleFunction<TravelSurveyZone> attributeDataGetter() {
		return tsz -> segments.parallelStream().mapToDouble(seg -> seg.attributeDataGetter().applyAsDouble(tsz)).sum();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder("Combined segment: ");
		segments.parallelStream().forEach(seg -> sb.append(seg.toString()+","));
		return sb.toString();
	}

	@Override
	public int hashCode() {
		if(hash == 0) {
			hash = toString().hashCode();
		}
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		try {
			CombinedMarketSegment other = (CombinedMarketSegment) obj;
			return other.segments.equals(segments);
		} catch (ClassCastException e) {
			return false;
		}
	}
}
