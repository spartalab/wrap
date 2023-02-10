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

import edu.utexas.wrap.TimePeriod;
import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.TravelSurveyZone;

public class ODMatrixCollector implements Collector<ODMatrix, CombinedODMatrix, ODMatrix>{
	private final Set<Characteristics> characteristics = new HashSet<Characteristics>(Arrays.asList(Collector.Characteristics.values()));
	private TimePeriod tp;
	private Mode mode;
	
	public ODMatrixCollector(Mode mode, TimePeriod tp) {
		this.mode = mode;
		this.tp = tp;
	}
	
	public ODMatrixCollector() {
		
	}

	@Override
	public Supplier<CombinedODMatrix> supplier() {
		return () -> new CombinedODMatrix(mode,tp);
	}

	@Override
	public BiConsumer<CombinedODMatrix, ODMatrix> accumulator() {
		return (parent, child) -> parent.add(child);
	}

	@Override
	public BinaryOperator<CombinedODMatrix> combiner() {
		return (mtx1, mtx2) ->{
			mtx2.getChildren().forEach(child -> mtx1.add(child));
			return mtx1;
		};
	}

	@Override
	public Function<CombinedODMatrix, ODMatrix> finisher() {
		return (od) -> (ODMatrix) od;
	}

	@Override
	public Set<Characteristics> characteristics() {
		return characteristics;
	}

	
}

class CombinedODMatrix implements ODMatrix {
	private Collection<ODMatrix> children;
	private TimePeriod tp;
	private Mode mode;
	
	public CombinedODMatrix(Mode mode, TimePeriod tp) {
		children = new HashSet<ODMatrix>();
		this.mode = mode;
		this.tp = tp;
	}
	
	public CombinedODMatrix() {
		this(null, null);
	}

	
	public void add(ODMatrix child) {
		children.add(child);
	}
	
	public Collection<ODMatrix> getChildren(){
		return children;
	}
	
	@Override
	public Mode getMode() {
		return mode;
	}

	@Override
	public float getDemand(TravelSurveyZone origin, TravelSurveyZone destination) {
		return (float) children.stream().mapToDouble(mtx -> mtx.getDemand(origin, destination)).sum();
	}

	@Override
	public void put(TravelSurveyZone origin, TravelSurveyZone destination, Float demand) {
		throw new RuntimeException("Writing to a read-only matrix");
	}

	@Override
	public TimePeriod timePeriod() {
		return tp;
	}

	@Override
	public Collection<TravelSurveyZone> getZones() {
		return children.stream().map(ODMatrix::getZones).findAny().orElseThrow(RuntimeException::new);
	}

	@Override
	public DemandMap getDemandMap(TravelSurveyZone origin) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Not yet implemented");
	}
	
	public double getTotalDemand() {
		return children.stream().mapToDouble(mtx -> mtx.getTotalDemand()).sum();
	}


}