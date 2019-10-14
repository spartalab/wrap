package edu.utexas.wrap.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;

public class DemandMapCollector implements Collector<DemandMap, CombinedDemandMap, DemandMap> {
	private final Set<Characteristics> characteristics = new HashSet<Characteristics>(Arrays.asList(Collector.Characteristics.values()));

	@Override
	public Supplier<CombinedDemandMap> supplier() {
		return () -> new CombinedDemandMap();
	}

	@Override
	public BiConsumer<CombinedDemandMap, DemandMap> accumulator() {
		return (parent,child) -> parent.add(child);
	}

	@Override
	public BinaryOperator<CombinedDemandMap> combiner() {
		return (map1,map2) -> {
			map2.getChildren().forEach(child -> map1.add(child));
			return map1;
		};
	}

	@Override
	public Function<CombinedDemandMap, DemandMap> finisher() {
		return (map) -> (DemandMap) map;
	}

	@Override
	public Set<Characteristics> characteristics() {
		return characteristics;
	}

}

class CombinedDemandMap implements DemandMap {

	private Collection<DemandMap> children;
	
	public CombinedDemandMap() {
		children = Collections.synchronizedSet(new HashSet<DemandMap>());
	}
	
	public Collection<DemandMap> getChildren() {
		return children;
	}

	public void add(DemandMap child) {
		children.add(child);
	}

	public CombinedDemandMap(Collection<DemandMap> maps) {
		this.children = maps;
	}
	
	@Override
	public Double get(TravelSurveyZone dest) {
		return children.parallelStream().mapToDouble(map -> map.get(dest)).sum();
	}

	@Override
	public Graph getGraph() {
		return children.parallelStream().map(DemandMap::getGraph).findAny().get();
	}

	@Override
	public Collection<TravelSurveyZone> getZones() {
		return children.parallelStream().flatMap(map -> map.getZones().parallelStream()).collect(Collectors.toSet());
	}

	@Override
	public Double getOrDefault(TravelSurveyZone node, Double f) {
		//FIXME this doesn't handle the default case correctly
		return children.parallelStream().mapToDouble(map -> map.get(node)).sum();
	}

	@Override
	public Double put(TravelSurveyZone dest, Double demand) {
		throw new RuntimeException("Writing to read-only map");
	}

	@Override
	public boolean isEmpty() {
		return !children.parallelStream().filter(map -> !map.isEmpty()).findAny().isPresent();
	}

	@Override
	public Map<TravelSurveyZone, Double> doubleClone() {
		return getZones().parallelStream().collect(Collectors.toMap(Function.identity(), zone -> get(zone).doubleValue()));
	}
	
}