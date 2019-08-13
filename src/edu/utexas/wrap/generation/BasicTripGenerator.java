package edu.utexas.wrap.generation;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import edu.utexas.wrap.marketsegmentation.MarketSegment;
import edu.utexas.wrap.net.AreaClass;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;

public class BasicTripGenerator {
	Graph g;
	
	public BasicTripGenerator(Graph graph) {
		g = graph;
	}
	
	public Map<TravelSurveyZone,Double> generate(MarketSegment segment){
		return g.getTSZs().parallelStream().collect(	//For each TSZ in parallel,
				Collectors.toMap(Function.identity(), tsz ->	//the TSZ maps to a value:
					//The data rate for this market segment times the market segment's value for this TSZ
						segment.getRate()*segment.getAttributeData().applyAsDouble(tsz)));
	}
	
	public Map<TravelSurveyZone,Double> scale(Map<TravelSurveyZone,Double> input, Map<AreaClass,Double> areaData){
		return input.entrySet().parallelStream().collect(	//For each input key-value mapping in parallel,
				Collectors.toMap(Entry::getKey, //Key maps to key,
						entry -> entry.getValue()*areaData.get( //Multiply the original value by the area factor
								entry.getKey().getAreaClass()	//for this TSZ's area class
								)));
	}
	
	public Map<TravelSurveyZone,Double> generateAndScale(MarketSegment segment, Map<AreaClass,Double> areaData){
		return scale(generate(segment),areaData);
	}
}
