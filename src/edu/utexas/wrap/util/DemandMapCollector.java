/*
    wrap - free and open-source urban transportation modeling software
    Copyright (C) 2017 the wrap project, The University of Texas at Austin

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
	public float get(TravelSurveyZone dest) {
		return (float) children.parallelStream().mapToDouble(map -> map.get(dest)).sum();
	}

	@Override
	public Collection<TravelSurveyZone> getZones() {
		return children.parallelStream().flatMap(map -> map.getZones().parallelStream()).collect(Collectors.toSet());
	}

	@Override
	public Float put(TravelSurveyZone dest, Float demand) {
		throw new RuntimeException("Writing to read-only map");
	}

	@Override
	public boolean isEmpty() {
		return !children.parallelStream().filter(map -> !map.isEmpty()).findAny().isPresent();
	}

	@Override
	public Map<TravelSurveyZone, Double> doubleClone() {
		return getZones().parallelStream().collect(Collectors.toMap(Function.identity(), zone -> (double) get(zone)));
	}
	
}