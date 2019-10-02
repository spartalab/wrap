package edu.utexas.wrap.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
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

public class ODMatrixCollector<T extends ODMatrix> implements Collector<ODMatrix, CombinedODMatrix, ODMatrix>{
	private final Set<Characteristics> characteristics = new HashSet<Characteristics>(Arrays.asList(Collector.Characteristics.values()));

	@Override
	public Supplier<CombinedODMatrix> supplier() {
		return () -> new CombinedODMatrix();
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
		// TODO Auto-generated method stub
		return characteristics;
	}

	
}

class CombinedODMatrix implements ODMatrix {
	private Collection<ODMatrix> children;
	
	public CombinedODMatrix() {
		children = new HashSet<ODMatrix>();
	}
	
	public void add(ODMatrix child) {
		children.add(child);
	}
	
	public Collection<ODMatrix> getChildren(){
		return children;
	}
	
	@Override
	public Mode getMode() {
		return children.parallelStream().map(ODMatrix::getMode).distinct().findAny().orElse(null);
	}

	@Override
	public Float getDemand(TravelSurveyZone origin, TravelSurveyZone destination) {
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
	public void write(Path outputOD) {
		try (BufferedWriter out = Files.newBufferedWriter(outputOD, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)){
			getGraph().getTSZs().parallelStream().forEach( orig -> {
						getGraph().getTSZs().parallelStream().filter(dest -> getDemand(orig,dest) > 0)
						.forEach(dest ->{
							try {
								StringBuilder sb = new StringBuilder();
								sb.append(orig.getNode().getID());
								sb.append(",");
								sb.append(dest.getNode().getID());
								sb.append(",");
								sb.append(getDemand(orig,dest));
								sb.append("\r\n");
								out.write(sb.toString());
								out.flush();
								//FIXME debug code for removal
								if (sb.toString().contains("9105952") && !sb.toString().contains(",")) {
									System.out.println();
								}
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						});
					});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}