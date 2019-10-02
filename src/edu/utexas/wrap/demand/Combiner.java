package edu.utexas.wrap.demand;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;

public class Combiner {

	public static Map<TravelSurveyZone,Float> totalProductions(Graph g, Collection<PAMap> maps) {
		return g.getTSZs().parallelStream().collect(
				Collectors.toMap(Function.identity(), tsz->
					(float) maps.parallelStream().mapToDouble(map -> map.getProductions(tsz)).sum()
						)
				);
	}
	
	public static Map<TravelSurveyZone,Float> totalAttractions(Graph g, Collection<PAMap> maps) {
		return g.getTSZs().parallelStream().collect(
				Collectors.toMap(Function.identity(), tsz->
					(float) maps.parallelStream().mapToDouble(map -> map.getAttractions(tsz)).sum()
						)
				);
	}
	
	public static PAMap combineMaps(Graph g, Collection<PAMap> maps) {
		return new PAMap() {
			Map<TravelSurveyZone,Float> productions = totalProductions(g,maps);
			Map<TravelSurveyZone,Float> attractions = totalAttractions(g,maps);
			
			@Override
			public Set<TravelSurveyZone> getProducers() {
				return productions.keySet();
			}

			@Override
			public Set<TravelSurveyZone> getAttractors() {
				return attractions.keySet();
			}

			@Override
			public Float getAttractions(TravelSurveyZone z) {
				return attractions.get(z);
			}

			@Override
			public Float getProductions(TravelSurveyZone z) {
				return productions.get(z);
			}

			@Override
			public Graph getGraph() {
				return g;
			}

			@Override
			public void putAttractions(TravelSurveyZone z, Float amt) {
				attractions.put(z, amt);				
			}

			@Override
			public void putProductions(TravelSurveyZone z, Float amt) {
				productions.put(z,amt);				
			}

			@Override
			public Float getVOT() {
				return null;
			}
			
		};
	}
	
	public static ModalPAMatrix combineModalMatrices(Graph g, Collection<ModalPAMatrix> matrices) {
		return new ModalPAMatrix() {

			@Override
			public Graph getGraph() {
				return g;
			}

			@Override
			public void put(TravelSurveyZone producer, TravelSurveyZone attractor, Float demand) {
				// TODO Auto-generated method stub
				throw new RuntimeException("Writing to a read-only matrix");
			}

			@Override
			public Float getDemand(TravelSurveyZone producer, TravelSurveyZone attractor) {
				return (float) matrices.parallelStream().mapToDouble(mtx -> mtx.getDemand(producer, attractor)).sum();
			}

			@Override
			public DemandMap getDemandMap(TravelSurveyZone producer) {
				// TODO Auto-generated method stub
				throw new RuntimeException("Not yet implemented");
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
				return matrices.parallelStream().flatMap(mtx -> mtx.getProducers().parallelStream()).collect(Collectors.toSet());
			}

			@Override
			public Mode getMode() {
				// TODO Auto-generated method stub
				throw new RuntimeException("Not yet implemented");
			}
		};
	}


	public static AggregatePAMatrix combineAggregateMatrices(Graph g, Collection<AggregatePAMatrix> values) {
		return new AggregatePAMatrix() {
			
			@Override
			public Graph getGraph() {
				return g;
			}

			@Override
			public void put(TravelSurveyZone producer, TravelSurveyZone attractor, Float demand) {
				throw new RuntimeException("Writing to read-only matrix");
			}

			@Override
			public Float getDemand(TravelSurveyZone producer, TravelSurveyZone attractor) {
				return (float) values.parallelStream().mapToDouble(mtx -> mtx.getDemand(producer, attractor)).sum();
			}

			@Override
			public DemandMap getDemandMap(TravelSurveyZone producer) {
				// TODO Auto-generated method stub
				Collection<DemandMap> maps = values.parallelStream().map(mtx -> mtx.getDemandMap(producer)).collect(Collectors.toSet());
				
				return new DemandMap() {

					@Override
					public Float get(TravelSurveyZone dest) {
						// TODO Auto-generated method stub
						return (float) maps.parallelStream().mapToDouble(map -> map.get(dest)).sum();
					}

					@Override
					public Graph getGraph() {
						// TODO Auto-generated method stub
						return g;
					}

					@Override
					public Collection<TravelSurveyZone> getZones() {
						// TODO Auto-generated method stub
						return maps.parallelStream().flatMap(map -> map.getZones().parallelStream()).collect(Collectors.toSet());
					}

					@Override
					public Float getOrDefault(TravelSurveyZone node, float f) {
						//FIXME this doesn't handle the default case correctly
						return (float) maps.parallelStream().mapToDouble(map -> map.get(node)).sum();
					}

					@Override
					public Float put(TravelSurveyZone dest, Float demand) {
						// TODO Auto-generated method stub
						throw new RuntimeException("Writing to read-only map");
					}

					@Override
					public boolean isEmpty() {
						return !maps.parallelStream().filter(map -> !map.isEmpty()).findAny().isPresent();
					}

					@Override
					public Map<TravelSurveyZone, Double> doubleClone() {
						// TODO Auto-generated method stub
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
				return values.parallelStream().flatMap(mtx -> mtx.getProducers().parallelStream()).collect(Collectors.toSet());
			}
			
		};
	}
	



}
