package edu.utexas.wrap;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import edu.utexas.wrap.balancing.Prod2AttrProportionalBalancer;
import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.Combiner;
import edu.utexas.wrap.demand.ModalPAMatrix;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.demand.containers.FixedMultiplierPassthroughPAMap;
import edu.utexas.wrap.demand.containers.PerProductionZoneMultiplierPassthroughMatrix;
import edu.utexas.wrap.distribution.FrictionFactorMap;
import edu.utexas.wrap.distribution.GravityDistributor;
import edu.utexas.wrap.distribution.TripDistributor;
import edu.utexas.wrap.marketsegmentation.IncomeGroupSegment;
import edu.utexas.wrap.marketsegmentation.IncomeGroupSegmenter;
import edu.utexas.wrap.marketsegmentation.IndustryClass;
import edu.utexas.wrap.marketsegmentation.MarketSegment;
import edu.utexas.wrap.modechoice.FixedProportionSplitter;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.modechoice.TripInterchangeSplitter;
import edu.utexas.wrap.net.AreaClass;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;
import edu.utexas.wrap.util.*;
import edu.utexas.wrap.util.io.*;

public class wrapNCTCOG {

	public static void main(String[] args) {
		try{

			Graph graph = readNetworkData(args);
			
			Collection<MarketSegment> segments;
			
			//Perform trip generation
			Map<TripPurpose,Map<MarketSegment,PAMap>> hbMaps = NCTCOGTripGen.tripGeneratorHNW(graph, segments);
			hbMaps.put(TripPurpose.HOME_WORK, NCTCOGTripGen.tripGeneratorHBW(graph, segments));
			
			//Perform trip balancing
			balance(graph, hbMaps);
			
			NHBThread nhb = new NHBThread(graph, hbMaps);
			nhb.start();
			
			//Peak/off-peak splitting for HOME_WORK trip purpose
			Map<MarketSegment, Double> splitRates = null;
			Map<MarketSegment, PAMap> pkMaps = splitHBW(hbMaps, splitRates);
			
			//Perform trip distribution
			Map<MarketSegment, FrictionFactorMap> pkFFMaps = null; //TODO
			Map<MarketSegment, AggregatePAMatrix> aggPKMtxs = peakDistribution(graph, pkMaps, pkFFMaps);	//TODO separate threading for distributing pkMaps
			
			
			Map<TripPurpose, Map<MarketSegment, FrictionFactorMap>> opFFMaps = null; //TODO
			Map<TripPurpose, Map<MarketSegment, AggregatePAMatrix>> aggOPMtxs = offPeakDistribution(graph, hbMaps, opFFMaps);
			
			//After distributing over different friction factor maps, the HBW trips are stuck back together and SRE & PBO matrices are combined
			Map<TripPurpose, Map<MarketSegment, AggregatePAMatrix>> aggCombinedMtxs = combineAggregateMatrices(aggPKMtxs,aggOPMtxs); //TODO combine SRE/PBO and HBWPK/OP matrices
			
			//TODO divide market segments further by vehicles per worker
			Map<MarketSegment, Map<MarketSegment, Map<TravelSurveyZone, Double>>> workerVehicleRates = null;
			Map<TripPurpose, Map<MarketSegment, AggregatePAMatrix>> dividedCombinedMtxs = subdivideSegments(aggCombinedMtxs,workerVehicleRates);
			
			//TODO combine HNW trip purposes into single HNW trip purpose for all market segments
			Map<TripPurpose, Map<MarketSegment, AggregatePAMatrix>> combinedMtxs = combineHNWPurposes(dividedCombinedMtxs);
			
			//Perform mode choice splitting TODO ensure proper use of market segments
			Map<MarketSegment, Map<Mode, Double>> modeShares = ModeFactory.readModeShares(new File("../../nctcogFiles/modeChoiceSplits.csv"), segments); // ModeChoiceSplits.csv
			Map<TripPurpose, Map<MarketSegment, Collection<ModalPAMatrix>>> hbModalMtxs = modeChoice(combinedMtxs, modeShares);
			
			//PA to OD splitting by time of day
			Map<Mode, Double> occupancyRates = ModeFactory.readOccRates(new File("../../nctcogFiles/modalOccRates.csv"), true); // modalOccRates.csv
			//TOD splitting inputs TODO ensure proper use of market segments
			Map<TimePeriod, Map<MarketSegment, Double>> 
				depRates = TimePeriodRatesFactory.readDepartureFile(new File("../../nctcogFiles/TODfactors.csv"), segments), //TODFactors.csv
				arrRates = TimePeriodRatesFactory.readArrivalFile(new File("../../nctcogFiles/TODfactors.csv"), segments); //TODFactors.csv
			Map<TimePeriod, Map<TripPurpose, Map<MarketSegment, Collection<ODMatrix>>>> hbODs = paToODConversion(hbModalMtxs, occupancyRates, depRates, arrRates);
			
			nhb.join();
			//Reduce the number of OD matrices by combining those of similar VOT
			Map<TimePeriod, Collection<ODMatrix>> reducedODs = reduceODMatrices(hbODs, nhb.getODs());
			
			//TODO figure out how to identify reduced ODs
			writeODs(reducedODs);
			
			//TODO eventually, we'll do multiple instances of traffic assignment here instead of just writing to files
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(2);
		}
	}
	
	private static Graph readNetworkData(String[] args) throws FileNotFoundException, IOException {
		//Model inputs
		Graph graph = GraphFactory.readEnhancedGraph(new File(args[0]),Integer.parseInt(args[1]));
		
		//TODO read RAAs - this is for later in the project
		
		// add demographic data to zones TODO consider whether this should be a method inside Graph
		//args[2] = "../../nctcogFiles/hhByIG.csv";
		//args[3] = "../../nctcogFiles/hhByIGthenWkrthenVeh.csv";
		//args[4] = "../../nctcogFiles/empByIGthenIC.csv"
		
		graph.readHouseholdsByIncomeGroup(Paths.get(args[2]));
		graph.readHouseholdsByWorkersVehiclesAndIncomeGroups(Paths.get(args[3]));
		graph.readEmploymentData(Paths.get(args[4]));
		
		return graph;
	}

	private static void balance(Graph g, Map<TripPurpose, Map<MarketSegment, PAMap>> hbMaps) {
		//TODO verify this behavior is correct
		Prod2AttrProportionalBalancer balancer = new Prod2AttrProportionalBalancer(null);
		hbMaps.values().parallelStream().flatMap(map -> map.values().parallelStream()).forEach(map -> balancer.balance(map));
	}
	
	private static Map<MarketSegment,PAMap> splitHBW(Map<TripPurpose,Map<MarketSegment,PAMap>> hbMaps, double pkShare) {
		Map<MarketSegment,PAMap> hbwMaps = hbMaps.get(TripPurpose.HOME_WORK);
		
		Map<MarketSegment,PAMap> pkMaps = new HashMap<MarketSegment,PAMap>();
		Map<MarketSegment,PAMap> opMaps = new HashMap<MarketSegment,PAMap>();
		
		hbwMaps.keySet().parallelStream().forEach(seg -> {
			PAMap whole = hbwMaps.get(seg);
			
			PAMap peak = new FixedMultiplierPassthroughPAMap(whole, pkShare);
			PAMap offpeak = new FixedMultiplierPassthroughPAMap(whole, 1-pkShare);
			
			opMaps.put(seg, offpeak);
			pkMaps.put(seg, peak);
		});
		
		hbMaps.put(TripPurpose.HOME_WORK, opMaps);
		return pkMaps;
	}
	
	private static Map<MarketSegment, AggregatePAMatrix> peakDistribution(
			Graph graph, 
			Map<MarketSegment, PAMap> pkMaps,
			Map<MarketSegment, FrictionFactorMap> pkFFMaps) {
		return pkMaps.entrySet().parallelStream().collect(Collectors.toMap(Entry::getKey, entry->{
			TripDistributor distributor = new GravityDistributor(graph, pkFFMaps.get(entry.getKey()));
			return distributor.distribute(entry.getValue());
		}));
	}

	private static Map<TripPurpose, Map<MarketSegment, AggregatePAMatrix>> offPeakDistribution(
			Graph g,
			Map<TripPurpose, Map<MarketSegment, PAMap>> hbMaps,
			Map<TripPurpose, Map<MarketSegment, FrictionFactorMap>> ffm 
			// TODO: consider if this (and pk distribution) should take in a mapping directly to the distributor 
			// (maybe some purpose-segment pairs have the same friction factor map? So this would be unnecessary) 
			) throws IOException {

		return hbMaps.entrySet().parallelStream().collect(Collectors.toMap(Entry::getKey, purposeEntry -> 
				purposeEntry.getValue().entrySet().parallelStream()
				.collect(Collectors.toMap(Entry::getKey, segmentEntry -> {
						TripDistributor distributor = new GravityDistributor(g, ffm.get(purposeEntry.getKey()).get(segmentEntry.getKey()));
						return distributor.distribute(segmentEntry.getValue());
					}))
				));
	}

	private static Map<TripPurpose, Map<MarketSegment, AggregatePAMatrix>> combineAggregateMatrices(
			Map<MarketSegment, AggregatePAMatrix> aggPKMtxs,
			Map<TripPurpose, Map<MarketSegment, AggregatePAMatrix>> aggMtxs) {

		Map<MarketSegment,AggregatePAMatrix> hbwOPMtxs = aggMtxs.get(TripPurpose.HOME_WORK);
		Map<MarketSegment,AggregatePAMatrix> hbwMtxs = new HashMap<MarketSegment,AggregatePAMatrix>();
		
		hbwOPMtxs.keySet().parallelStream().forEach(seg -> {
			AggregatePAMatrix pk = aggPKMtxs.get(seg);
			AggregatePAMatrix op = hbwOPMtxs.get(seg);

			AggregatePAMatrix combined = Stream.of(pk,op).collect(new AggregatePAMatrixCollector());
			hbwMtxs.put(seg, combined);
		});
		
		aggMtxs.put(TripPurpose.HOME_WORK, hbwMtxs);
		return aggMtxs;
	}
	
	private static Map<TripPurpose, Map<MarketSegment, AggregatePAMatrix>> subdivideSegments(Map<TripPurpose, Map<MarketSegment, AggregatePAMatrix>> aggCombinedMtxs,
			Map<MarketSegment, Map<MarketSegment, Map<TravelSurveyZone, Double>>> workerVehicleRates) {
		return aggCombinedMtxs.entrySet().parallelStream().collect(Collectors.toMap(Entry::getKey, purposeMapEntry-> //for each trip purpose
			 purposeMapEntry.getValue().entrySet().parallelStream() //Stream of MarketSegment-Matrix entries
			.map(oldSegmentMap ->	//Take each segment-matrix pair
					workerVehicleRates.get(oldSegmentMap.getKey()).entrySet().parallelStream()		//get a stream of all related segment-rateMap pairs
					.collect(Collectors.toMap(Entry::getKey, 										//map each related (more bespoke) segment to 
							rateMap ->  new PerProductionZoneMultiplierPassthroughMatrix(oldSegmentMap.getValue(),rateMap.getValue())))	//a rate multiplier map
			).flatMap(map -> map.entrySet().parallelStream()) 			//unpackage the map to a set of entries
			.collect(Collectors.toMap(Entry::getKey, Entry::getValue))	//collect all the entries together in a single map
			//This assumes no duplicates exist in the more bespoke (resulting) market segments
			
		));
		
		
	}

	private static Map<TripPurpose, Map<MarketSegment, AggregatePAMatrix>> combineHNWPurposes(Map<TripPurpose, Map<MarketSegment, AggregatePAMatrix>> aggCombinedMtxs) {
		Map<TripPurpose,Map<MarketSegment,AggregatePAMatrix>> ret = new HashMap<TripPurpose,Map<MarketSegment,AggregatePAMatrix>>();
		ret.put(TripPurpose.HOME_WORK, aggCombinedMtxs.get(TripPurpose.HOME_WORK));
		ret.put(TripPurpose.HOME_NONWORK, 
			aggCombinedMtxs.entrySet().parallelStream()							//For every trip purpose
				.filter(entry -> entry.getKey() != TripPurpose.HOME_WORK)		//Except home-based work
				.flatMap(entry -> entry.getValue().entrySet().parallelStream())	//Get a stream of its MarketSegment-Matrix pairings
				.collect(Collectors.groupingBy(									//Group them by their market segments
						Entry::getKey,Collectors.mapping(						//then map to the values
								Entry::getValue, new AggregatePAMatrixCollector()))));	//and combine together using a collector
		
		return ret;
	}

	private static Map<TripPurpose,Map<MarketSegment, Collection<ModalPAMatrix>>> modeChoice(
			Map<TripPurpose,Map<MarketSegment, AggregatePAMatrix>> aggMtxs, 
			Map<MarketSegment,Map<Mode,Double>> modeShares
			) throws IOException {
		TripInterchangeSplitter mc = new FixedProportionSplitter(modeShares);
		return aggMtxs.entrySet().parallelStream().collect(Collectors.toMap(Entry::getKey, purposeMapEntry ->
			purposeMapEntry.getValue().entrySet().parallelStream()
			.collect(Collectors.toMap(Entry::getKey, 
				entry -> mc.split(entry.getValue(),entry.getKey())
				.collect(Collectors.toSet())))
				));
	}
	
	private static Map<TimePeriod,Map<TripPurpose, Map<MarketSegment,Collection<ODMatrix>>>> paToODConversion(
			Map<TripPurpose, Map<MarketSegment, Collection<ModalPAMatrix>>> hbModalMtxs, 
			Map<Mode,Double> occupancyRates, 
			Map<TimePeriod,Map<MarketSegment,Double>> depRates,
			Map<TimePeriod,Map<MarketSegment,Double>> arrRates) throws IOException {
		
		//TODO combine SR2 and SR3
		return Stream.of(TimePeriod.values()).collect(Collectors.toMap(Function.identity(), tp -> //for each time period
			hbModalMtxs.entrySet().parallelStream().collect(Collectors.toMap(Entry::getKey, purposeEntry -> //for each trip purpose
				purposeEntry.getValue().entrySet().parallelStream().collect(Collectors.toMap(Entry::getKey, segmentEntry ->{ //for each market segment
					//establish a trip converter
					DepartureArrivalConverter converter = new DepartureArrivalConverter(depRates.get(tp).get(segmentEntry.getKey()), arrRates.get(tp).get(segmentEntry.getKey()));
					return segmentEntry.getValue().parallelStream().map(modalMtx -> //for each modal matrix
						converter.convert(modalMtx, occupancyRates.get(modalMtx.getMode()))	//convert the matrix
					).collect(Collectors.toSet());	//collect into a set 
				})) //which is collected into a map (for each market segment) 
			))	//which is collected into a map (for each trip purpose)
		));	//which is collected into a map (for each time period)
	}

	private static Map<TimePeriod, Collection<ODMatrix>> reduceODMatrices(
			Map<TimePeriod, Map<TripPurpose, Map<MarketSegment, Collection<ODMatrix>>>> hbODs,
			Map<TimePeriod, Map<TripPurpose, Collection<ODMatrix>>> nhbODs) {
		return Stream.of(TimePeriod.values()).parallel().collect(Collectors.toMap(Function.identity(), timePeriod ->{
			Collection<ODMatrix> ret = new HashSet<ODMatrix>();
			
			// TODO Auto-generated method stub
			Stream.of(TripPurpose.HOME_WORK,TripPurpose.HOME_NONWORK).parallel().forEach(tripPurpose->
				Stream.of(Mode.SINGLE_OCC, Mode.HOV).parallel().forEach(mode -> {
			
					//First, handle the Work IG123 cases
					ret.add(
						hbODs.get(timePeriod)	//add a combination of all OD matrices from this time period
						.get(tripPurpose).entrySet().parallelStream()	//for this trip purpose
						.filter(entry -> entry.getKey() instanceof IncomeGroupSegmenter && ((IncomeGroupSegmenter) entry.getKey()).getIncomeGroup() <4)	//in income groups 1, 2, or 3 
						.map(Entry::getValue).flatMap(Collection::parallelStream).filter(od -> od.getMode() == mode) //which matches this mode
						.collect(new ODMatrixCollector())
					);
			
					//Next, handle the Work IG4 and Nonwork cases
					ret.add(
							Stream.concat(	//Combine two types of maps
									hbODs.get(timePeriod)	//home-based trips from this time period
									.get(tripPurpose).entrySet().parallelStream()	//for this trip purpose
									.filter(entry -> entry.getKey() instanceof IncomeGroupSegmenter && ((IncomeGroupSegmenter) entry.getKey()).getIncomeGroup() >= 4)	//in income group 4
									.map(Entry::getValue).flatMap(Collection::parallelStream).filter(od -> od.getMode() == mode),	//for this mode
									
									nhbODs.get(timePeriod)	//And non-home-based trips
									.get(tripPurpose).parallelStream()	//for this trip purpose
									.filter(od -> od.getMode() == mode)	//using this mode
									)
							.collect(new ODMatrixCollector())
					);
				})
			);
			
			return ret;
		}));
	}
	
	private static void writeODs(Map<TimePeriod, Map<Mode, ODMatrix>> ods) {
		//TODO determine output files
		Map<TimePeriod,Map<Mode,Path>> outputODPaths = new HashMap<TimePeriod,Map<Mode,Path>>();
//		outputODPaths.put(TimePeriod.AM_PK, Paths.get("morningPeak.csv"));
//		outputODPaths.put(TimePeriod.PM_PK, Paths.get("eveningPeak.csv"));

		System.out.print("Writing OD matrices... ");
		//Write to file AM and PM peak OD matrices
		long ms = System.currentTimeMillis();
		
		ods.entrySet().parallelStream()
		.filter(todEntry -> 
//				todEntry.getKey().equals(TimePeriod.AM_PK) || 
			todEntry.getKey().equals(TimePeriod.PM_PK)
			)
		.forEach(todEntry -> todEntry.getValue().entrySet().parallelStream()
				.forEach(modeEntry -> 
					modeEntry.getValue().write(outputODPaths.get(todEntry.getKey()).get(modeEntry.getKey()))
					)
				);

		long nms = System.currentTimeMillis();
		System.out.println(""+(nms-ms)/1000.0+" s");
	}
	
}
