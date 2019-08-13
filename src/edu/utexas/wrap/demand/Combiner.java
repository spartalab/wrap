package edu.utexas.wrap.demand;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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
	
	public static PAMap mapCombiner(Graph g, Collection<PAMap> maps) {
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
}
