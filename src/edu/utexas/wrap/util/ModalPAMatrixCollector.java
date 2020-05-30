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

import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.ModalPAMatrix;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;

public class ModalPAMatrixCollector implements Collector<ModalPAMatrix,Collection<ModalPAMatrix>,ModalPAMatrix> {
	private final Set<Characteristics> characteristics = new HashSet<Characteristics>(Arrays.asList(Collector.Characteristics.CONCURRENT,Collector.Characteristics.UNORDERED));

	@Override
	public Function<Collection<ModalPAMatrix>,ModalPAMatrix> finisher(){
		return (PAs) -> new CombinedModalPAMatrix(PAs);
	}

	@Override
	public Supplier<Collection<ModalPAMatrix>> supplier() {
		return () -> new HashSet<ModalPAMatrix>();
	}

	@Override
	public BiConsumer<Collection<ModalPAMatrix>, ModalPAMatrix> accumulator() {
		return (collection, child) -> collection.add(child);
	}

	@Override
	public BinaryOperator<Collection<ModalPAMatrix>> combiner() {
		return (collection1, collection2) -> {
			collection1.addAll(collection2);
			return collection1;
		};
	}

	@Override
	public Set<Characteristics> characteristics() {
		return characteristics;
	}
}

class CombinedModalPAMatrix implements ModalPAMatrix {

	private Collection<ModalPAMatrix> children;
	
	public CombinedModalPAMatrix(Collection<ModalPAMatrix> pas) {
		children = pas;
	}
	
	@Override
	public Graph getGraph() {
		return children.parallelStream().map(ModalPAMatrix::getGraph).findAny().get();
	}

	@Override
	public void put(TravelSurveyZone producer, TravelSurveyZone attractor, Float demand) {
		throw new RuntimeException("Writing to read-only matrix");
	}

	@Override
	public Float getDemand(TravelSurveyZone producer, TravelSurveyZone attractor) {
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

	@Override
	public Mode getMode() {
		return children.parallelStream().map(ModalPAMatrix::getMode).findAny().get();
	}
	
}