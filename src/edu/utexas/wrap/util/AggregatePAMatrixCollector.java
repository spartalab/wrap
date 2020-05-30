package edu.utexas.wrap.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;

public class AggregatePAMatrixCollector implements Collector<AggregatePAMatrix, Collection<AggregatePAMatrix>, AggregatePAMatrix> {
	private final Set<Characteristics> characteristics = new HashSet<Characteristics>(Arrays.asList(Collector.Characteristics.CONCURRENT,Collector.Characteristics.UNORDERED));
	
	@Override
	public BiConsumer<Collection<AggregatePAMatrix>, AggregatePAMatrix> accumulator() {
		return (collection, child) -> collection.add(child);
	}

	@Override
	public Set<Characteristics> characteristics() {
		return characteristics;
	}

	@Override
	public BinaryOperator<Collection<AggregatePAMatrix>> combiner() {
		return (mtx1, mtx2) -> {
			mtx1.addAll(mtx2);
			return mtx1;
		};
	}

	@Override
	public Function<Collection<AggregatePAMatrix>, AggregatePAMatrix> finisher() {
		return (PAs) -> new CombinedAggregatePAMatrix(PAs) ;
	}

	@Override
	public Supplier<Collection<AggregatePAMatrix>> supplier() {
		return () -> new HashSet<AggregatePAMatrix>();
	}

}

class CombinedAggregatePAMatrix implements AggregatePAMatrix {

	private Collection<AggregatePAMatrix> children;
	
	public CombinedAggregatePAMatrix(Collection<AggregatePAMatrix> pas) {
		children = pas;
	}
	
	@Override
	public Graph getGraph() {
		return children.parallelStream().map(AggregatePAMatrix::getGraph).findAny().get();
	}

	@Override
	public void put(TravelSurveyZone producer, TravelSurveyZone attractor, Float demand) {
		throw new RuntimeException("Writing to read-only matrix");
	}

	@Override
	public Float getDemand(TravelSurveyZone producer, TravelSurveyZone attractor) {
//		System.out.println(children.size());
		return (float) children.stream().mapToDouble(mtx -> mtx.getDemand(producer, attractor)).sum();
	}

	@Override
	public DemandMap getDemandMap(TravelSurveyZone producer) {

		Collection<DemandMap> maps = children.parallelStream().map(mtx -> mtx.getDemandMap(producer)).collect(Collectors.toSet());
		
		return new CombinedDemandMap(maps);
	}

	@Override
	public Collection<TravelSurveyZone> getProducers() {
		return children.parallelStream().flatMap(mtx -> mtx.getProducers().parallelStream()).collect(Collectors.toSet());
	}
	
}