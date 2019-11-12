package edu.utexas.wrap.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collector.Characteristics;
import java.util.stream.Collectors;

import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;

public class PAMapCollector implements Collector<PAMap, CombinedPAMap, PAMap>{
	private final Set<Characteristics> characteristics = new HashSet<Characteristics>(Arrays.asList(Collector.Characteristics.values()));

	@Override
	public Supplier<CombinedPAMap> supplier() {
		return () -> new CombinedPAMap();
	}

	@Override
	public BiConsumer<CombinedPAMap, PAMap> accumulator() {
		return (parent, child) -> parent.add(child);
	}

	@Override
	public BinaryOperator<CombinedPAMap> combiner() {
		return (map1,map2) ->{
			map2.getChildren().forEach(child -> map1.add(child));
			return map1;
		};
	}

	@Override
	public Function<CombinedPAMap, PAMap> finisher() {
		return (pa) -> (PAMap) pa;
	}

	@Override
	public Set<Characteristics> characteristics() {
		return characteristics;
	}

}

class CombinedPAMap implements PAMap {
	private Collection<PAMap> children;
	
	public CombinedPAMap() {
		children = Collections.synchronizedSet(new HashSet<PAMap>());
	}

	public boolean add(PAMap child) {
		return children.add(child);
	}
	
	public Collection<PAMap> getChildren(){
		return children;
	}
	
	@Override
	public Collection<TravelSurveyZone> getProducers() {
		return children.parallelStream().flatMap(map -> map.getProducers().parallelStream()).collect(Collectors.toSet());
	}

	@Override
	public Collection<TravelSurveyZone> getAttractors() {
		return children.parallelStream().flatMap(map -> map.getAttractors().parallelStream()).collect(Collectors.toSet());
	}

	@Override
	public Float getAttractions(TravelSurveyZone z) {
		return (float) children.parallelStream().mapToDouble(map -> map.getAttractions(z)).sum();
	}

	@Override
	public Float getProductions(TravelSurveyZone z) {
		return (float) children.parallelStream().mapToDouble(map -> map.getProductions(z)).sum();
	}

	@Override
	public Graph getGraph() {
		return children.parallelStream().map(PAMap::getGraph).findAny().get();
	}

	@Override
	public void putAttractions(TravelSurveyZone z, Float amt) {
		throw new RuntimeException("Writing to read-only map");
	}

	@Override
	public void putProductions(TravelSurveyZone z, Float amt) {
		throw new RuntimeException("Writing to read-only map");
	}

	@Override
	public DemandMap getProductionMap() {
		// TODO Auto-generated method stub
		throw new RuntimeException();
	}

	@Override
	public DemandMap getAttractionMap() {
		// TODO Auto-generated method stub
		throw new RuntimeException();
	}
	
}