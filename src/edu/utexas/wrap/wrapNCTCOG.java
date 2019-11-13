package edu.utexas.wrap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import edu.utexas.wrap.balancing.Prod2AttrProportionalBalancer;
import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.demand.containers.PAPassthroughMap;
import edu.utexas.wrap.generation.AreaSpecificTripGenerator;
import edu.utexas.wrap.generation.BasicTripGenerator;
import edu.utexas.wrap.marketsegmentation.IncomeGroupSegment;
import edu.utexas.wrap.marketsegmentation.IncomeGroupSegmenter;
import edu.utexas.wrap.marketsegmentation.MarketSegment;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.AreaClass;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.util.DemandMapCollector;
import edu.utexas.wrap.util.ODMatrixCollector;
import edu.utexas.wrap.util.io.ODMatrixWriter;

public class wrapNCTCOG {

	public static void main(String[] args) {
		try{
			ModelInput model = new ModelInputNCTCOG("inputs.properties");
			Graph graph = model.getNetwork();
			
			//Perform trip generation
			Map<TripPurpose,Map<MarketSegment,PAMap>> hbMaps = generateTrips(graph, model);
			
			//Perform trip balancing
			balance(graph, hbMaps);
			
			NHBThread nhb = new NHBThread(graph, model, hbMaps);
			nhb.start();
			
			HBThread hb = new HBThread(graph, hbMaps);
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
			writeODs(reducedODs, model.getOutputDirectory());
			
			//TODO eventually, we'll do multiple instances of traffic assignment here instead of just writing to files
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(2);
		}
	}
	
	private static Map<TripPurpose,Map<MarketSegment, PAMap>> generateTrips(Graph g, ModelInput model) throws IOException {
		

		Map<TripPurpose,Map<MarketSegment,Double>> prodRates = Stream.of(
				TripPurpose.HOME_WORK,
				TripPurpose.HOME_SHOP,
				TripPurpose.HOME_SRE,
				TripPurpose.HOME_PBO,
				TripPurpose.HOME_K12).parallel().collect(Collectors.toMap(Function.identity(), purpose -> model.getGeneralProdRates(purpose)));

		Map<TripPurpose,Map<MarketSegment,Map<AreaClass,Double>>> attrRates = prodRates.keySet().parallelStream()
				.collect(Collectors.toMap(Function.identity(), purpose -> model.getAreaClassAttrRates(purpose)));

		//Generate primary productions
		Map<TripPurpose,Map<MarketSegment, DemandMap>> primaryProds = prodRates.keySet().parallelStream()
				.collect(Collectors.toMap(Function.identity(), purpose -> generateProductions(g, prodRates.get(purpose))));
		//Generate primary attractions
		Map<TripPurpose,Map<MarketSegment, DemandMap>> primaryAttrs = attrRates.keySet().parallelStream()
				.collect(Collectors.toMap(Function.identity(), purpose -> generateAttractions(g, attrRates.get(purpose))));

		//Combine primary maps across non-income-group segments
		Map<TripPurpose,Map<MarketSegment,DemandMap>> combinedProds = primaryProds.entrySet().parallelStream()
				.collect(Collectors.toMap(Entry::getKey, entry -> combineMapsByIncomeGroupSegment(entry.getValue())));
		Map<TripPurpose,Map<MarketSegment,DemandMap>> combinedAttrs = primaryAttrs.entrySet().parallelStream()
				.collect(Collectors.toMap(Entry::getKey, entry-> combineMapsByIncomeGroupSegment(entry.getValue())));

		return combinedProds.entrySet().parallelStream().collect(Collectors.toMap(Entry::getKey, 
				purposeEntry -> Stream.concat(
						purposeEntry.getValue().keySet().parallelStream(),
						combinedAttrs.get(purposeEntry.getKey()).keySet().parallelStream()
						).distinct().collect(Collectors.toMap(Function.identity(),
						segment -> new PAPassthroughMap(g,purposeEntry.getValue().get(segment), combinedAttrs.get(purposeEntry.getKey()).get(segment))))));
	}

	/** This method combines a set of MarketSegment-DemandMap pairs together 
	 * based on their income group market segment
	 * @param afterProdSegs the set of income group market segments that should be retained
	 * @param demandMaps the maps from more fine-grained market segmentations to their demand maps
	 * @return a reduced MarketSegment-DemandMap pair where all values have the same income group as the key
	 */
	private static Map<MarketSegment, DemandMap> combineMapsByIncomeGroupSegment(Map<MarketSegment, DemandMap> demandMaps) {
		return IntStream.range(1, 5).parallel().boxed().map(ig -> new IncomeGroupSegment(ig)).collect(Collectors.toMap(Function.identity(), newSeg ->
			
			 demandMaps.entrySet().parallelStream()
					.filter(entry -> entry.getKey() instanceof IncomeGroupSegmenter && ((IncomeGroupSegmenter) entry.getKey()).getIncomeGroup() == ((IncomeGroupSegmenter) newSeg).getIncomeGroup())
					.map(Entry::getValue)
					.collect(new DemandMapCollector())
		));
	}

	
	private static Map<MarketSegment, DemandMap> generateAttractions(Graph g, Map<MarketSegment, Map<AreaClass, Double>> attrRates) {
		AreaSpecificTripGenerator generator = new AreaSpecificTripGenerator(g,attrRates);
		return attrRates.keySet().parallelStream().collect(Collectors.toMap(Function.identity(), seg -> generator.generate(seg)));
	}

	private static Map<MarketSegment, DemandMap> generateProductions(Graph g, Map<MarketSegment, Double> prodRates) {
		BasicTripGenerator generator = new BasicTripGenerator(g,prodRates);
		return prodRates.keySet().parallelStream().collect(Collectors.toMap(Function.identity(), seg ->  generator.generate(seg)));
	}
	
	private static void balance(Graph g, Map<TripPurpose, Map<MarketSegment, PAMap>> hbMaps) {
		//TODO verify this behavior is correct
		Prod2AttrProportionalBalancer balancer = new Prod2AttrProportionalBalancer(null);
		hbMaps.values().parallelStream().flatMap(map -> map.values().parallelStream()).forEach(map -> balancer.balance(map));
	}

	
	private static Map<TimePeriod, Collection<ODMatrix>> reduceODMatrices(
			Map<TimePeriod, Map<TripPurpose, Map<MarketSegment, Collection<ODMatrix>>>> hbODs,
			Map<TimePeriod, Map<TripPurpose, Collection<ODMatrix>>> nhbODs) {
		return Stream.of(TimePeriod.values()).parallel().collect(Collectors.toMap(Function.identity(), timePeriod ->{
			Collection<ODMatrix> ret = new HashSet<ODMatrix>();
			
			Stream.of(TripPurpose.HOME_WORK,TripPurpose.HOME_NONWORK).parallel().forEach(tripPurpose->
				Stream.of(Mode.SINGLE_OCC, Mode.HOV).parallel().forEach(mode -> {
			
					//First, handle the Work IG123 cases
					ret.add(
						hbODs.get(timePeriod)	//add a combination of all OD matrices from this time period
						.get(tripPurpose).entrySet().parallelStream()	//for this trip purpose
						.filter(entry -> entry.getKey() instanceof IncomeGroupSegmenter && ((IncomeGroupSegmenter) entry.getKey()).getIncomeGroup() <4)	//in income groups 1, 2, or 3 
						.map(Entry::getValue).flatMap(Collection::parallelStream).filter(od -> od.getMode() == mode || (mode == Mode.HOV && (od.getMode() == Mode.HOV_2_PSGR || od.getMode() == Mode.HOV_3_PSGR))) //which matches this mode
						.collect(new ODMatrixCollector())
					);
			
					//Next, handle the Work IG4 and Nonwork cases
					ret.add(
							Stream.concat(	//Combine two types of maps
									hbODs.get(timePeriod)	//home-based trips from this time period
									.get(tripPurpose).entrySet().parallelStream()	//for this trip purpose
									.filter(entry -> entry.getKey() instanceof IncomeGroupSegmenter && ((IncomeGroupSegmenter) entry.getKey()).getIncomeGroup() >= 4)	//in income group 4
									.map(Entry::getValue).flatMap(Collection::parallelStream).filter(od -> od.getMode() == mode || (mode == Mode.HOV && (od.getMode() == Mode.HOV_2_PSGR || od.getMode() == Mode.HOV_3_PSGR))),	//for this mode
									
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
	
	
	private static void writeODs(Map<TimePeriod, Collection<ODMatrix>> ods, String outputDir) {
		
		ods.entrySet().parallelStream().forEach(todEntry -> todEntry.getValue().parallelStream().forEach(matrix -> ODMatrixWriter.write(outputDir,todEntry.getKey(), matrix)));
	}
	
}
