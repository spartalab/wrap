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
			// Make this a subthread
			HBThread hb = new HBThread(graph, pkMaps, segments);
			hb.start();

			try {
				hb.join();
				nhb.join();
			} catch(InterruptedException e) {
				System.out.println("Thread is interrupted.\n");
			}

			//Reduce the number of OD matrices by combining those of similar VOT
			Map<TimePeriod, Collection<ODMatrix>> reducedODs = reduceODMatrices(hb.getODs(), nhb.getODs());
			
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
