package edu.utexas.wrap.util;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
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

public class AggregatePAMatrixCollector<T extends AggregatePAMatrix> implements Collector<AggregatePAMatrix, CombinedAggregatePAMatrix, AggregatePAMatrix> {
	private final Set<Characteristics> characteristics = new HashSet<Characteristics>(Arrays.asList(Collector.Characteristics.values()));
	
	@Override
	public BiConsumer<CombinedAggregatePAMatrix, AggregatePAMatrix> accumulator() {
		return (parent, child) -> parent.add(child);
	}

	@Override
	public Set<Characteristics> characteristics() {
		return characteristics;
	}

	@Override
	public BinaryOperator<CombinedAggregatePAMatrix> combiner() {
		return (mtx1, mtx2) -> {
			mtx2.getChildren().forEach(child -> mtx1.add(child));
			return mtx1;
		};
	}

	@Override
	public Function<CombinedAggregatePAMatrix, AggregatePAMatrix> finisher() {
		return (pa) -> (AggregatePAMatrix) pa;
	}

	@Override
	public Supplier<CombinedAggregatePAMatrix> supplier() {
		return () -> new CombinedAggregatePAMatrix();
	}

}

class CombinedAggregatePAMatrix implements AggregatePAMatrix {

	private Collection<AggregatePAMatrix> children;
	
	public CombinedAggregatePAMatrix() {
		children = new HashSet<AggregatePAMatrix>();
	}
	
	@Override
	public Graph getGraph() {
		return children.parallelStream().map(AggregatePAMatrix::getGraph).findAny().get();
	}

	public Object add(AggregatePAMatrix child) {
		return children.add(child);
	}
	
	public Collection<AggregatePAMatrix> getChildren(){
		return children;
	}

	
	@Override
	public void put(TravelSurveyZone producer, TravelSurveyZone attractor, Float demand) {
		throw new RuntimeException("Writing to read-only matrix");
	}

	@Override
	public Float getDemand(TravelSurveyZone producer, TravelSurveyZone attractor) {
		return (float) children.parallelStream().mapToDouble(mtx -> mtx.getDemand(producer, attractor)).sum();
	}

	@Override
	public DemandMap getDemandMap(TravelSurveyZone producer) {

		Collection<DemandMap> maps = children.parallelStream().map(mtx -> mtx.getDemandMap(producer)).collect(Collectors.toSet());
		
		return new DemandMap() {

			@Override
			public Float get(TravelSurveyZone dest) {
				return (float) maps.parallelStream().mapToDouble(map -> map.get(dest)).sum();
			}

			@Override
			public Graph getGraph() {
				return children.parallelStream().map(AggregatePAMatrix::getGraph).findAny().get();
			}

			@Override
			public Collection<TravelSurveyZone> getZones() {
				return maps.parallelStream().flatMap(map -> map.getZones().parallelStream()).collect(Collectors.toSet());
			}

			@Override
			public Float getOrDefault(TravelSurveyZone node, float f) {
				//FIXME this doesn't handle the default case correctly
				return (float) maps.parallelStream().mapToDouble(map -> map.get(node)).sum();
			}

			@Override
			public Float put(TravelSurveyZone dest, Float demand) {
				throw new RuntimeException("Writing to read-only map");
			}

			@Override
			public boolean isEmpty() {
				return !maps.parallelStream().filter(map -> !map.isEmpty()).findAny().isPresent();
			}

			@Override
			public Map<TravelSurveyZone, Double> doubleClone() {
				return getZones().parallelStream().collect(Collectors.toMap(Function.identity(), zone -> get(zone).doubleValue()));
			}
		};
	}

	@Override
	public float getVOT() {
		// TODO Auto-generated method stub
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public void toFile(File out) throws IOException {
		// TODO Auto-generated method stub
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public Collection<TravelSurveyZone> getProducers() {
		return children.parallelStream().flatMap(mtx -> mtx.getProducers().parallelStream()).collect(Collectors.toSet());
	}
	
}