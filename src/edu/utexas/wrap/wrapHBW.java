package edu.utexas.wrap;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
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
import edu.utexas.wrap.distribution.FrictionFactorMap;
import edu.utexas.wrap.distribution.GravityDistributor;
import edu.utexas.wrap.distribution.TripDistributor;
import edu.utexas.wrap.marketsegmentation.IncomeGroupSegment;
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

public class wrapHBW {

	public static void main(String[] args) {
		try{

			Graph graph = readNetworkData(args);
			Map<TripPurpose,Map<MarketSegment,FrictionFactorMap>> opFFMaps = null; //TODO
			Map<MarketSegment,FrictionFactorMap> pkFFMaps = null; //TODO
			Map<TravelSurveyZone,Double> workerVehicleRates = null;
			
			//Perform trip generation
			Map<TripPurpose,Map<MarketSegment,PAMap>> hbMaps = NCTCOGTripGen.tripGeneratorHNW(graph);
			hbMaps.put(TripPurpose.HOME_WORK, NCTCOGTripGen.tripGeneratorHBW(graph));
			
			//Perform trip balancing
			balance(graph, hbMaps);
			
			//NHB thread starts here
			//TODO New thread should start here for non-home-based trips
			Map<TripPurpose,PAMap> nhbMaps = NCTCOGTripGen.tripGeneratorNHB(graph,hbMaps);
			nhbBalance(graph, nhbMaps);
			Map<TripPurpose,AggregatePAMatrix> nhbMatrices = nhbTripDist(nhbMaps);
			combineNHBPurposes(nhbMatrices);
			Map<TripPurpose,Collection<ModalPAMatrix>> nhbModalMtxs = nhbModeChoice(nhbMatrices);
			Map<TripPurpose,Collection<ODMatrix>> nhbODs = nhbPA2OD(nhbModalMtxs);
			//NHB thread ends here

			
			//Peak/off-peak splitting
			Map<MarketSegment,PAMap> pkMaps = splitHBW(hbMaps, 0.5); //TODO this method should both reduce the hbMaps' HBW entries by a half and return a duplicate with the same reduction
			//Perform trip distribution
			Map<MarketSegment,AggregatePAMatrix> aggPKMtxs = peakDistribution(graph, pkMaps, pkFFMaps);	//TODO separate threading for distributing pkMaps
			Map<TripPurpose,Map<MarketSegment,AggregatePAMatrix>> aggOPMtxs = offPeakDistribution(graph, hbMaps, opFFMaps);
			//After distributing over different friction factor maps, the HBW trips are stuck back together and SRE & PBO matrices are combined
			Map<TripPurpose,Map<MarketSegment,AggregatePAMatrix>> aggCombinedMtxs = combineAggregateMatrices(aggPKMtxs,aggOPMtxs); //TODO combine SRE/PBO and HBWPK/OP matrices
			//TODO divide market segments further by vehicles per worker
			divideSegments(aggCombinedMtxs,workerVehicleRates);
			//TODO combine HNW trip purposes into single HNW trip purpose for all market segments
			Map<TripPurpose,Map<MarketSegment,AggregatePAMatrix>> combinedMtxs = combineHNWPurposes(aggCombinedMtxs);
			
			//Perform mode choice splitting
			Map<TripPurpose,Map<MarketSegment, Collection<ModalPAMatrix>>> hbModalMtxs = modeChoice(combinedMtxs);
			
			//PA to OD splitting by time of day
			Map<TripPurpose,Map<TimePeriod, Map<Mode, ODMatrix>>> hbODs = paToODConversion(hbModalMtxs);
			
			//Reduce the number of OD matrices by combining those of similar VOT
			Map<TimePeriod,Collection<ODMatrix>> reducedODs = reduceODMatrices(hbODs, nhbODs);
			
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
		System.out.print("Reading network... ");
		long ms = System.currentTimeMillis();
		//Model inputs
		Graph graph = GraphFactory.readEnhancedGraph(new File(args[0]),Integer.parseInt(args[1]));
		long nms = System.currentTimeMillis();
		System.out.println(""+(nms-ms)/1000.0+" s");
		
		//TODO read RAAs
		// add demographic data to zones
		System.out.print("Reading household demographic data... ");
		ms = System.currentTimeMillis();
		readHouseholdData(graph, Paths.get("../../nctcogFiles/hhByIG.csv"), Paths.get("../../nctcogFiles/hhByIGthenWkrthenVeh.csv"));
		nms = System.currentTimeMillis();
		System.out.println(""+(nms-ms)/1000.0+" s");
		
		
		System.out.print("Reading employment demographic data... ");
		ms = System.currentTimeMillis();
		readEmploymentData(graph, Paths.get("../../nctcogFiles/empByIGthenIC.csv"));
		nms = System.currentTimeMillis();
		System.out.println(""+(nms-ms)/1000.0+" s");
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

	private static Map<MarketSegment, Map<TimePeriod,FrictionFactorMap>> readFrictionFactorMaps(
			Collection<MarketSegment> afterPASegments, Graph graph) throws IOException {
		float[][] skim = SkimFactory.readSkimFile(new File("../../nctcogFiles/PKNOHOV.csv"), false, graph);
		Map<MarketSegment, FrictionFactorMap> ffmaps = new ConcurrentHashMap<MarketSegment, FrictionFactorMap>();
		String[] ff_files = {
				"../../nctcogFiles/FFactorHBW_INC1 PK.csv",
				"../../nctcogFiles/FFactorHBW_INC2 PK.csv",
				"../../nctcogFiles/FFactorHBW_INC3 PK.csv"};
		
		afterPASegments.parallelStream().forEach(seg -> { // This could be made a lot cleaner...
					int idx = 0;
					while(idx < ff_files.length && !ff_files[idx].contains(((IncomeGroupSegment) seg).getIncomeGroup() + "")){
						idx++;
					}
					try {
						ffmaps.put(seg, FrictionFactorFactory.readFactorFile(new File(ff_files[idx]), true, skim));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
		);
		return ffmaps;
	}
	
	private static void readHouseholdData(Graph graph, Path igFile, Path igWkrVehFile) throws IOException {
		// TODO Auto-generated method stub
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
		System.out.print("Performing trip balancing... ");
		long ms = System.currentTimeMillis();
	
		Prod2AttrProportionalBalancer balancer = new Prod2AttrProportionalBalancer(null);
		hbMaps.values().parallelStream().forEach(map -> balancer.balance(map));
		
		long nms = System.currentTimeMillis();
		System.out.println(""+(nms-ms)/1000.0+" s");
	}

	private static Map<MarketSegment, Map<TimePeriod, PAMap>> pkOpSplitting(Map<MarketSegment, PAMap> maps,
			Collection<MarketSegment> segments) throws IOException {
		System.out.print("Performing peak-offpeak splitting... ");
		Map<MarketSegment,Double> pkRates = PeakFactory.readPkOPkSplitRates(new File("../../nctcogFiles/pkOffPkSplits.csv"), true, segments); // pkOffPkSplits.csv
		return maps.entrySet().parallelStream().collect(Collectors.toMap(Entry::getKey, 
				entry -> {
					double pkRate = pkRates.get(entry.getKey());
					PAMap pkMap = new FixedMultiplierPassthroughPAMap(entry.getValue(),pkRate);
					PAMap opMap = new FixedMultiplierPassthroughPAMap(entry.getValue(),1.0-pkRate);
					Map<TimePeriod,PAMap> ret = new HashMap<TimePeriod,PAMap>(3,1.0f);
					ret.put(TimePeriod.AM_PK, pkMap);
					ret.put(TimePeriod.EARLY_OP, opMap);
					return ret;
				}));
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

	private static Map<MarketSegment, Collection<ModalPAMatrix>> modeChoice(Collection<MarketSegment> segments,
			Map<MarketSegment, AggregatePAMatrix> aggMtxs) throws IOException {
		Map<MarketSegment,Map<Mode,Double>> modeShares = ModeFactory.readModeShares(new File("../../nctcogFiles/modeChoiceSplits.csv"), segments); // ModeChoiceSplits.csv
		TripInterchangeSplitter mc = new FixedProportionSplitter(modeShares);
		return aggMtxs.entrySet().parallelStream()
				.collect(Collectors.toMap(Entry::getKey, 
						entry -> mc.split(entry.getValue(),entry.getKey())
						.collect(Collectors.toSet())));
	}
	
	private static Map<TimePeriod, Map<Mode,ODMatrix>> paToODConversion(Map<MarketSegment, Collection<ModalPAMatrix>> modalMtxs) throws IOException {
		
		
		
		System.out.print("Reading modal shares and occupancy rates... ");
		//Mode choice inputs
		long ms = System.currentTimeMillis();
		Map<Mode,Double> occupancyRates = ModeFactory.readOccRates(new File("../../nctcogFiles/modalOccRates.csv"), true); // modalOccRates.csv
		long nms = System.currentTimeMillis();
		System.out.println(""+(nms-ms)/1000.0+" s");
		
		
		System.out.print("Reading time-of-day rates... ");
		//TOD splitting inputs
		ms = System.currentTimeMillis();
		Map<MarketSegment, Map<TimePeriod,Double>> depRates = TimePeriodRatesFactory.readDepartureFile(new File("../../nctcogFiles/TODfactors.csv"), modalMtxs.keySet()), //TODFactors.csv
				   arrRates = TimePeriodRatesFactory.readArrivalFile(new File("../../nctcogFiles/TODfactors.csv"), modalMtxs.keySet()); //TODFactors.csv
		nms = System.currentTimeMillis();
		System.out.println(""+(nms-ms)/1000.0+" s");
		
		
		return Stream.of(TimePeriod.values()).parallel()			
		.collect(Collectors.toMap(Function.identity(), tp -> {
			
			//Convert using TOD splitting
			Stream<ODMatrix> tpODs = modalMtxs.entrySet().parallelStream()
					.flatMap(entry -> {
						DepartureArrivalConverter converter = new DepartureArrivalConverter(depRates.get(entry.getKey()).get(tp),arrRates.get(entry.getKey()).get(tp));
						return entry.getValue().parallelStream().map(mtx -> converter.convert(mtx, occupancyRates.get(mtx.getMode())));
					});
			
			//Combine across income groups 1,2,3 and vehicle ownership
			//FIXME this loses market segmentation
//			return tpODs.collect(Collectors.partitioningBy(od -> od.getMode().equals(Mode.SINGLE_OCC), od -> Combiner.combineODMatrices(od));
			return tpODs.collect(Collectors.groupingBy(ODMatrix::getMode, new ODMatrixCollector<ODMatrix>()));
		}));
//		return null;
	}
}
