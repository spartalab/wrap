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

import edu.utexas.wrap.balancing.Attr2ProdProportionalBalancer;
import edu.utexas.wrap.balancing.Prod2AttrProportionalBalancer;
import edu.utexas.wrap.balancing.TripBalancer;
import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.demand.containers.FixedSizePAMap;
import edu.utexas.wrap.demand.containers.PAPassthroughMap;
import edu.utexas.wrap.generation.AreaSpecificTripGenerator;
import edu.utexas.wrap.generation.BasicTripGenerator;
import edu.utexas.wrap.marketsegmentation.IncomeGroupSegment;
import edu.utexas.wrap.marketsegmentation.IncomeGroupSegmenter;
import edu.utexas.wrap.marketsegmentation.MarketSegment;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.AreaClass;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.util.ODMatrixCollector;
import edu.utexas.wrap.util.PAMapCollector;
import edu.utexas.wrap.util.io.ODMatrixWriter;

public class wrapNCTCOG {

	public static long startMS = System.currentTimeMillis();
	public static void main(String[] args) {
		try{
			ModelInput model = new ModelInputNCTCOG("inputs.properties");
			System.out.print((System.currentTimeMillis()-wrapNCTCOG.startMS)+" ms\t");
			System.out.println("Reading network");
			Graph graph = model.getNetwork();
			
			//Perform trip generation
			System.out.print((System.currentTimeMillis()-wrapNCTCOG.startMS)+" ms\t");
			System.out.println("Generating primary trips");
			
			Map<TripPurpose, Map<MarketSegment, Double>> prodRates = getProdRates(model);
			//Generate primary productions
			Map<TripPurpose, Map<MarketSegment, DemandMap>> primaryProds = getProds(graph, prodRates);
			
			NHBThread nhb = new NHBThread(graph, model, primaryProds);
			nhb.start();
						
			Map<TripPurpose, Map<MarketSegment, Map<AreaClass, Double>>> attrRates = getAttrRates(model);
			//Generate primary attractions
			Map<TripPurpose, Map<MarketSegment, DemandMap>> primaryAttrs = getAttrs(graph, attrRates);

			Map<TripPurpose,Map<MarketSegment,PAMap>> hbMaps = combine(graph, primaryProds, primaryAttrs);
			


			hbMaps = hbMaps.entrySet().parallelStream().collect(Collectors.toMap(Entry::getKey, entry -> combineMapsByIncomeGroupSegment(entry.getValue())));
			
			
			System.out.print((System.currentTimeMillis()-wrapNCTCOG.startMS)+" ms\t");
			System.out.println("Flattening primary production-attraction maps");
			hbMaps = flatten(hbMaps);
			
			//Perform trip balancing
			System.out.print((System.currentTimeMillis()-wrapNCTCOG.startMS)+" ms\t");
			System.out.println("Balancing primary production-attraction maps");
			balance(graph, hbMaps);	
			
			
			HBThread hb = new HBThread(graph, model, hbMaps);
			hb.start();

			try {
				hb.join();
				nhb.join();
			} catch(InterruptedException e) {
				System.out.println("Thread is interrupted.\n");
			}

			//Reduce the number of OD matrices by combining those of similar VOT
			System.out.print((System.currentTimeMillis()-wrapNCTCOG.startMS)+" ms\t");
			System.out.println("Reducing OD matrices");
			Map<TimePeriod, Collection<ODMatrix>> reducedODs = reduceODMatrices(hb.getODs(), nhb.getODs());
			
			//TODO figure out how to identify reduced ODs
			System.out.print((System.currentTimeMillis()-wrapNCTCOG.startMS)+" ms\t");
			System.out.println("Writing to files");
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
	
	private static Map<TripPurpose,Map<MarketSegment,PAMap>> flatten(Map<TripPurpose,Map<MarketSegment,PAMap>> maps) {
		return maps.entrySet().parallelStream().collect(Collectors.toMap(Entry::getKey, purposeEntry ->
			purposeEntry.getValue().entrySet().parallelStream().collect(Collectors.toMap(Entry::getKey, segmentEntry->
				new FixedSizePAMap(segmentEntry.getValue())
			))
		));
	}
	
	private static Map<TripPurpose, Map<MarketSegment, PAMap>> combine(Graph g,
			Map<TripPurpose, Map<MarketSegment, DemandMap>> primaryProds,
			Map<TripPurpose, Map<MarketSegment, DemandMap>> primaryAttrs) {
		return primaryProds.entrySet().parallelStream().collect(Collectors.toMap(Entry::getKey, 
				purposeEntry -> Stream.concat(
						purposeEntry.getValue().keySet().parallelStream(),
						primaryAttrs.get(purposeEntry.getKey()).keySet().parallelStream()
						).distinct().collect(Collectors.toMap(Function.identity(),
						segment -> new PAPassthroughMap(g,purposeEntry.getValue().get(segment), primaryAttrs.get(purposeEntry.getKey()).get(segment))))));
	}

	private static Map<TripPurpose, Map<MarketSegment, DemandMap>> getAttrs(Graph g,
			Map<TripPurpose, Map<MarketSegment, Map<AreaClass, Double>>> attrRates) {
		Map<TripPurpose,Map<MarketSegment, DemandMap>> primaryAttrs = attrRates.keySet().parallelStream()
				.collect(Collectors.toMap(Function.identity(), purpose -> generateAttractions(g, attrRates.get(purpose))));
		return primaryAttrs;
	}

	private static Map<TripPurpose, Map<MarketSegment, Map<AreaClass, Double>>> getAttrRates(ModelInput model) {
		Map<TripPurpose,Map<MarketSegment,Map<AreaClass,Double>>> attrRates =  getHBPurposes()
				.collect(Collectors.toMap(Function.identity(), purpose -> model.getAreaClassAttrRates(purpose)));
		return attrRates;
	}

	private static Stream<TripPurpose> getHBPurposes() {
		return Stream.of(
				TripPurpose.HOME_WORK,
				TripPurpose.HOME_SHOP,
				TripPurpose.HOME_SRE,
//				TripPurpose.HOME_K12,
				TripPurpose.HOME_PBO
				).parallel();
	}

	private static Map<TripPurpose, Map<MarketSegment, DemandMap>> getProds(Graph g,
			Map<TripPurpose, Map<MarketSegment, Double>> prodRates) {
		Map<TripPurpose,Map<MarketSegment, DemandMap>> primaryProds = prodRates.keySet().parallelStream()
				.collect(Collectors.toMap(Function.identity(), purpose -> generateProductions(g, prodRates.get(purpose))));
		return primaryProds;
	}

	private static Map<TripPurpose, Map<MarketSegment, Double>> getProdRates(ModelInput model) {
		Map<TripPurpose,Map<MarketSegment,Double>> prodRates = getHBPurposes().collect(Collectors.toMap(Function.identity(), purpose -> model.getGeneralProdRates(purpose)));
		return prodRates;
	}

	/** This method combines a set of MarketSegment-DemandMap pairs together 
	 * based on their income group market segment
	 * @param afterProdSegs the set of income group market segments that should be retained
	 * @param demandMaps the maps from more fine-grained market segmentations to their demand maps
	 * @return a reduced MarketSegment-DemandMap pair where all values have the same income group as the key
	 */
	private static Map<MarketSegment, PAMap> combineMapsByIncomeGroupSegment(Map<MarketSegment, PAMap> paMaps) {
		return IntStream.range(1, 5).parallel().boxed().map(ig -> new IncomeGroupSegment(ig)).collect(Collectors.toMap(Function.identity(), newSeg ->
			
			 paMaps.entrySet().parallelStream()
					.filter(entry -> entry.getKey() instanceof IncomeGroupSegmenter && ((IncomeGroupSegmenter) entry.getKey()).getIncomeGroup() == ((IncomeGroupSegmenter) newSeg).getIncomeGroup())
					.map(Entry::getValue)
					.collect(new PAMapCollector())
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
		hbMaps.entrySet().parallelStream().forEach(entry -> {
			TripBalancer balancer = entry.getKey().equals(TripPurpose.HOME_WORK)? 
					new Prod2AttrProportionalBalancer(null) : new Attr2ProdProportionalBalancer();

			entry.getValue().values().parallelStream().forEach(paMap -> balancer.balance(paMap));
		});
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
