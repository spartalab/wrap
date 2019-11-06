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
			
			//NHB thread starts here
			//TODO New thread should start here for non-home-based trips
			Map<TripPurpose,PAMap> nhbMaps = NCTCOGTripGen.tripGeneratorNHB(graph,hbMaps);
			nhbBalance(graph, nhbMaps);
			Map<TripPurpose,AggregatePAMatrix> nhbMatrices = nhbTripDist(nhbMaps, nhbFFMaps);
			combineNHBPurposes(nhbMatrices);
			Map<TripPurpose,Collection<ModalPAMatrix>> nhbModalMtxs = nhbModeChoice(nhbMatrices);
			Map<TimePeriod,Map<TripPurpose,Collection<ODMatrix>>> nhbODs = nhbPA2OD(nhbModalMtxs);
			//NHB thread ends here

			
			//Peak/off-peak splitting
			Map<MarketSegment, PAMap> pkMaps = splitHBW(hbMaps, 0.5); //TODO this method should both reduce the hbMaps' HBW entries by a half and return a duplicate with the same reduction
			
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
			
			//Reduce the number of OD matrices by combining those of similar VOT
			Map<TimePeriod, Collection<ODMatrix>> reducedODs = reduceODMatrices(hbODs, nhbODs);
			
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

	private static Graph readNetworkData(String[] args) throws FileNotFoundException, IOException {
		//Model inputs
		Graph graph = GraphFactory.readEnhancedGraph(new File(args[0]),Integer.parseInt(args[1]));
		
		//TODO read RAAs - this is for later in the project
		
		// add demographic data to zones TODO consider whether this should be a method inside Graph
		//args[2] = "../../nctcogFiles/hhByIG.csv";
		//args[3] = "../../nctcogFiles/hhByIGthenWkrthenVeh.csv";
		
		readHouseholdData(graph, Paths.get(args[2]), Paths.get(args[3]));
		readEmploymentData(graph, Paths.get("../../nctcogFiles/empByIGthenIC.csv"));
		
		return graph;
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

//	private static Map<MarketSegment, Map<TimePeriod,FrictionFactorMap>> readFrictionFactorMaps(
//			Collection<MarketSegment> afterPASegments, 
//			Map<TimePeriod,float[][]> skim,
//			Graph graph) throws IOException {
//		float[][] skim = SkimFactory.readSkimFile(new File("../../nctcogFiles/PKNOHOV.csv"), false, graph);
//		Map<MarketSegment, FrictionFactorMap> ffmaps = new ConcurrentHashMap<MarketSegment, FrictionFactorMap>();
//		String[] ff_files = {
//				"../../nctcogFiles/FFactorHBW_INC1 PK.csv",
//				"../../nctcogFiles/FFactorHBW_INC2 PK.csv",
//				"../../nctcogFiles/FFactorHBW_INC3 PK.csv"};
//		
//		afterPASegments.parallelStream().forEach(seg -> { // This could be made a lot cleaner...
//					int idx = 0;
//					while(idx < ff_files.length && !ff_files[idx].contains(((IncomeGroupSegment) seg).getIncomeGroup() + "")){
//						idx++;
//					}
//					try {
//						ffmaps.put(seg, FrictionFactorFactory.readFactorFile(new File(ff_files[idx]), true, skim));
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//				}
//		);
//		return ffmaps;
//	}
	
	private static void readHouseholdData(Graph graph, Path igFile, Path igWkrVehFile) throws IOException {
		Files.lines(igFile).parallel().filter(line -> !line.startsWith("TSZ")).forEach(line ->{
			String[] args = line.split(",");
			int tszID = Integer.parseInt(args[0]);
			
			Map<Integer, Double> hhByIG = IntStream.range(1, 4).parallel().boxed().collect(
					Collectors.toMap(Function.identity(), ig -> Double.parseDouble(args[ig])));
			
			TravelSurveyZone tsz = graph.getNode(tszID).getZone();
			tsz.setHouseholdsByIncomeGroup(hhByIG);
		});
		
		Files.lines(igWkrVehFile).parallel().filter(line -> !line.startsWith("TSZ")).forEach(line ->{
			String[] args = line.split(",");
			int tszID = Integer.parseInt(args[0]);
			

			if (args.length < 72) {
				args = new String[]{args[0],"0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0","0"};
			}
			String[] newArgs = args;
			
			TravelSurveyZone tsz = graph.getNode(tszID).getZone();

			IntStream.range(1, 65).parallel().boxed().forEach(idx ->{
				double val = Double.parseDouble(newArgs[idx]);
				int wkr, veh, ig;
				ig = (idx-1) % 4 + 1;
				veh = ((idx-1)/4) % 4;
				wkr = ((idx-1)/16) % 4;
				tsz.setHouseholdsByIncomeGroupThenWorkersThenVehicles(ig, wkr, veh, val);
			});
		});
	}

	private static void readEmploymentData(Graph graph, Path file) throws IOException {
		Files.lines(file).parallel().filter(line -> !line.startsWith("TSZ")).forEach(line -> {
			String[] args = line.split(",");
			
			if (args.length < 11) {
				args = new String[]{args[0],args[1],"0","0","0","0","0","0","0","0","0"};
			}
			int tszID = Integer.parseInt(args[0]);
			AreaClass ac;
			switch (Integer.parseInt(args[1])) {
			case 1:
				ac = AreaClass.CBD;
				break;
			case 2:
				ac = AreaClass.OBD;
				break;
			case 3:
				ac = AreaClass.URBAN_RESIDENTIAL;
				break;
			case 4:
				ac = AreaClass.SUBURBAN_RESIDENTIAL;
				break;
			case 5:
				ac = AreaClass.RURAL;
				break;
			default:
				throw new RuntimeException("Unknown area type");
			}
			
			Map<Integer,Map<IndustryClass,Double>> empByIGthenIC = new HashMap<Integer,Map<IndustryClass,Double>>();
			Map<IndustryClass,Double> ig1 = new HashMap<IndustryClass,Double>();
			ig1.put(IndustryClass.BASIC, Double.parseDouble(args[2]));
			ig1.put(IndustryClass.RETAIL, Double.parseDouble(args[3]));
			ig1.put(IndustryClass.SERVICE, Double.parseDouble(args[4]));
			empByIGthenIC.put(1, ig1);
			
			Map<IndustryClass,Double> ig2 = new HashMap<IndustryClass,Double>();
			ig2.put(IndustryClass.BASIC, Double.parseDouble(args[5]));
			ig2.put(IndustryClass.RETAIL, Double.parseDouble(args[6]));
			ig2.put(IndustryClass.SERVICE, Double.parseDouble(args[7]));
			empByIGthenIC.put(2, ig2);
			
			Map<IndustryClass,Double> ig3 = new HashMap<IndustryClass,Double>();
			ig3.put(IndustryClass.BASIC, Double.parseDouble(args[8]));
			ig3.put(IndustryClass.RETAIL, Double.parseDouble(args[9]));
			ig3.put(IndustryClass.SERVICE, Double.parseDouble(args[10]));
			empByIGthenIC.put(3, ig3);
			
			TravelSurveyZone tsz = graph.getNode(tszID).getZone();
			tsz.setAreaClass(ac);
			tsz.setEmploymentByIncomeGroupThenIndustry(empByIGthenIC);
		});
	}

	private static void balance(Graph g, Map<TripPurpose, Map<MarketSegment, PAMap>> hbMaps) {
	
		Prod2AttrProportionalBalancer balancer = new Prod2AttrProportionalBalancer(null);
		hbMaps.values().parallelStream().flatMap(map -> map.values().parallelStream()).forEach(map -> balancer.balance(map));
		
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
}
