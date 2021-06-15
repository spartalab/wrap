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
import edu.utexas.wrap.demand.containers.FixedSizeDemandMap;
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
	
	private final DemandMap[] demandMaps;
	private final Collection<TravelSurveyZone> zones;
	
	public CombinedAggregatePAMatrix(Collection<AggregatePAMatrix> pas) {
		zones = pas.stream().findAny().get().getZones();
		demandMaps = new DemandMap[zones.size()];
		zones.stream().forEach(producer ->{
			DemandMap dm = new FixedSizeDemandMap(zones);
			zones.stream()
			.forEach(attractor -> dm.put(attractor, 
					(float) pas.stream()
					.mapToDouble(pa -> pa.getDemand(producer, attractor)).sum())
					);
			demandMaps[producer.getOrder()] = dm;
		});
	}
	
	@Override
	public Collection<TravelSurveyZone> getZones() {
		return zones;
	}

	@Override
	public void put(TravelSurveyZone producer, TravelSurveyZone attractor, Float demand) {
		throw new RuntimeException("Writing to read-only matrix");
	}

	@Override
	public float getDemand(TravelSurveyZone producer, TravelSurveyZone attractor) {
//		System.out.println(children.size());
		return demandMaps[producer.getOrder()].get(attractor);
	}

	@Override
	public DemandMap getDemandMap(TravelSurveyZone producer) {

		return demandMaps[producer.getOrder()];
	}

	@Override
	public Collection<TravelSurveyZone> getProducers() {
		return zones.stream().filter(zone -> !demandMaps[zone.getOrder()].isEmpty()).collect(Collectors.toSet());
	}
	
}