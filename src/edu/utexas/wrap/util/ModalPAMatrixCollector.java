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

import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.ModalPAMatrix;
import edu.utexas.wrap.modechoice.Mode;
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
	public void put(TravelSurveyZone producer, TravelSurveyZone attractor, Float demand) {
		throw new RuntimeException("Writing to read-only matrix");
	}

	@Override
	public float getDemand(TravelSurveyZone producer, TravelSurveyZone attractor) {
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