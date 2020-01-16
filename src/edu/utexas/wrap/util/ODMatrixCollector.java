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

import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;

public class ODMatrixCollector implements Collector<ODMatrix, CombinedODMatrix, ODMatrix>{
	private final Set<Characteristics> characteristics = new HashSet<Characteristics>(Arrays.asList(Collector.Characteristics.values()));
	private double vot;
	private Mode mode;
	
	public ODMatrixCollector(Mode mode, Double vot) {
		this.mode = mode;
		this.vot = vot;
	}
	
	public ODMatrixCollector() {
		
	}

	@Override
	public Supplier<CombinedODMatrix> supplier() {
		return () -> new CombinedODMatrix(mode,vot);
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
	private Float vot;
	private Mode mode;
	
	public CombinedODMatrix(Mode mode, Double vot) {
		children = new HashSet<ODMatrix>();
		this.vot = vot == null? 0.0f : vot.floatValue();
		this.mode = mode;
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
		return (float) children.parallelStream().mapToDouble(mtx -> mtx.getDemand(origin, destination)).sum();
	}

	@Override
	public void put(TravelSurveyZone origin, TravelSurveyZone destination, Float demand) {
		throw new RuntimeException("Writing to a read-only matrix");
	}

	@Override
	public Graph getGraph() {
		return children.parallelStream().map(ODMatrix::getGraph).findAny().get();
	}

	@Override
	public Float getVOT() {
		return vot;
	}

	@Override
	public void setVOT(float VOT) {
		vot = VOT;
	}
}