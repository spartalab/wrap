package edu.utexas.wrap;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.function.ToDoubleFunction;
import java.util.stream.IntStream;

import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.Combiner;
import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.generation.BasicTripGenerator;
import edu.utexas.wrap.marketsegmentation.ChildSegment;
import edu.utexas.wrap.marketsegmentation.EducationClass;
import edu.utexas.wrap.marketsegmentation.MarketSegment;
import edu.utexas.wrap.marketsegmentation.StudentSegment;
import edu.utexas.wrap.marketsegmentation.WorkerHouseholdSizeSegment;
import edu.utexas.wrap.marketsegmentation.WorkerSegment;
import edu.utexas.wrap.marketsegmentation.WorkerVehicleSegment;
import edu.utexas.wrap.net.AreaClass;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;

public class NCTCOGTripGeneration {
	Graph g;
	
	public NCTCOGTripGeneration() {
		
		Collection<AggregatePAMatrix> workBasedWork,	//Work trips generated as a result of a home-based work trip
		workBasedESH,	//Entertainment/shopping trips generated as a result of a home-based work trip 
		workBasedOther,	//Other trips generated as a result of a home-based work trip
		educationBased,	//Trips generated as a result of a home-based education (K-12/College) trip
		shoppingBasedShopping,	//Shopping trips generated as a result of a home-based shopping trip
		shoppingBasedOther,	//Other trips generated as a result of a home-based shopping trip
		otherBased,		//Trips generated as a result of some other home-based trip
		
		nonHomeBased;	//Trips that aren't generated as a secondary trip
		

		//Need educational rate for k12, comm college, university
		//Need home attraction rate for each trip purpose (8)
		//need map from area type to map from industry to map from trip type to data rate		
		
		//For each zone 
			//Need TSZ data on industry (basic, retail, service) and area type
			//Need TSZ number of households
			//Need TSZ attendance data for elementary, middle, high, comm college, university
			
			//If the zone is not an airport, calculate employment trips
				//get the zone's area type and its associated rates for each industry-trip type pair
				//each trip purpose-industry pair's attractions are calculated as the trip purpose rate * the employment in this industry in the zone
		
			//If there is school attendance, calculate education trips (overwrite NHB EDU trips from employment)
				//for each of k12, cc, and univ, the attractions are just the rate times the enrollment (k12 is combined enrollment across elem, mid, high)
		
			//Calculate home attractions for each trip purpose (home attraction rate * number of households)
		
			//For each trip purpose, total attractions is sum of home attractions and all industry attractions
		
	}
	
	
	
	
	public PAMap homeBasedWorkProductions(BasicTripGenerator generator) {
		Map<Integer, Map<Integer, Double>> rates;
		
		IntStream.range(0,4).parallel().forEach(numWorkers -> {
			IntStream.range(0, 3).parallel().forEach(numVehicles -> {
				Map<TravelSurveyZone,Double> productions = generator.generate(new WorkerVehicleSegment(numWorkers,numVehicles, rates.get(numWorkers).get(numVehicles)));
			});
		});
		
		//We want the resultant maps segmented by workers and vehicles
	}
	
	public PAMap homeBasedShoppingProductions(BasicTripGenerator generator) {
		Map<AreaClass,Double> geoFactors;
		Map<Integer,Map<Integer,Double>> rates;
		
		IntStream.range(0, 4).parallel().forEach(numWorkers -> {
			IntStream.range(1, 5).parallel().forEach(householdSize -> {
				Map<TravelSurveyZone,Double> productions = generator.generateAndScale(new WorkerHouseholdSizeSegment(numWorkers,householdSize,rates.get(numWorkers).get(householdSize)),geoFactors);
						
			});
		});
		
		//We want the resultant maps segmented by workers and household size
	}
	
	public PAMap homeBasedCollegeProductions(BasicTripGenerator generator) {
		Map<AreaClass,Double> geoFactors;
		Map<Integer, Double> rates;
		
		IntStream.range(0, 4).parallel().forEach(numWorkers -> {
			Map<TravelSurveyZone,Double> productions = generator.generateAndScale(new WorkerSegment(numWorkers,rates.get(numWorkers)), geoFactors);
		});
		
		//We want the resultant maps segmented by workers
	}
	
	public PAMap homeBasedK12Productions(BasicTripGenerator generator) {
		Map<AreaClass,Double> geoFactors;
		Map<Integer, Double> rates;
		
		IntStream.range(0, 3).parallel().forEach(numChildren -> {
			Map<TravelSurveyZone,Double> productions = generator.generateAndScale(new ChildSegment(numChildren,rates.get(numChildren)),geoFactors);
		});
		
		//We want the resultant maps segmented by children
	}
	
	public PAMap homeBasedSREProductions(BasicTripGenerator generator) {
		Map<AreaClass,Double> geoFactors;
		Map<Integer, Double> rates;
		
		IntStream.range(0, 3).parallel().forEach(numChildren -> {
			Map<TravelSurveyZone,Double> productions = generator.generateAndScale(new ChildSegment(numChildren,rates.get(numChildren)), geoFactors);
		});
		
		//We want the resultant maps segmented by children
	}
	
	public PAMap homeBasedPBOProductions(BasicTripGenerator generator) {
		Map<AreaClass,Double> geoFactors;
		Map<Integer,Double> rates;
		
		IntStream.range(0, 3).parallel().forEach(numChildren -> {
			Map<TravelSurveyZone,Double> productions = generator.generateAndScale(new ChildSegment(numChildren,rates.get(numChildren)), geoFactors);
		});
		
		//We want the resultant maps segmented by children
	}
	
	public PAMap homeBasedCollegeAttractions(BasicTripGenerator generator) {
		Map<EducationClass,Double> rates;
		
		EnumSet.of(EducationClass.COLLEGE, EducationClass.UNIVERSITY).parallelStream().forEach(eduType ->{
			Map<TravelSurveyZone,Double> attractions = generator.generate(new StudentSegment(eduType,rates.get(eduType)));
		});
	}
}
