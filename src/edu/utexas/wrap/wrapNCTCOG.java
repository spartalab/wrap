package edu.utexas.wrap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
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
import edu.utexas.wrap.util.io.output.ODMatrixBINWriter;
import edu.utexas.wrap.util.io.output.ODMatrixCSVWriter;


public class wrapNCTCOG {

	public static long startMS = System.currentTimeMillis();
	
	public static void main(String[] args) {
		try{
			ModelInput model = new ModelInputNCTCOG("inputs.properties");
			printTimeStamp();
			System.out.println("Reading network");
			Graph graph = model.getNetwork();
			
			//Perform trip generation
			printTimeStamp();
			System.out.println("Generating primary trips");
			
			Map<TripPurpose, Map<MarketSegment, Double>> prodRates = getProdRates(model);
			//Generate primary productions
			Map<TripPurpose, Map<MarketSegment, DemandMap>> primaryProds = getProds(graph, prodRates);
			
			//Begin a separate thread for handling NHB trip generation, distribution, mode choice, and PA-to-OD conversion
			NHBThread nhb = new NHBThread(graph, model, primaryProds); 
			nhb.start();
						
			Map<TripPurpose, Map<MarketSegment, Map<AreaClass, Double>>> attrRates = getAttrRates(model);
			//Generate primary attractions
			Map<TripPurpose, Map<MarketSegment, DemandMap>> primaryAttrs = getAttrs(graph, attrRates);

			//Combine home-based productions and attractions into a PAMap structure
			Map<TripPurpose,Map<MarketSegment,PAMap>> hbMaps = combine(graph, primaryProds, primaryAttrs);
			

			//Combine all segments based on their income group
			hbMaps = hbMaps.entrySet().parallelStream().collect(
					Collectors.toMap(Entry::getKey, entry -> combineMapsByIncomeGroupSegment(entry.getValue())));
			
			printTimeStamp();
			System.out.println("Flattening primary production-attraction maps");
			hbMaps = flatten(hbMaps);
			
			printTimeStamp();
			System.out.println("Balancing primary production-attraction maps");
			balance(graph, hbMaps);	
			
			//Begin a separate thread for handling distribution, mode choice, and PA-to-OD conversion
			HBThread hb = new HBThread(graph, model, hbMaps);
			hb.start();

			//Wait for the two threads to finish
			try {
				hb.join();
				nhb.join();
			} catch(InterruptedException e) {
				System.err.println("Thread is interrupted.\n");
				e.printStackTrace();
				System.exit(8);
			}

			//Reduce the OD matrices by combining across several factors as defined in NCTCOG standard
			printTimeStamp();
			System.out.println("Reducing OD matrices");
			Map<TimePeriod, Collection<ODMatrix>> reducedODs = reduceODMatrices(hb.getODs(), nhb.getODs());
			
			//Write OD matrices to files
			printTimeStamp();
			System.out.println("Writing to files");
			writeODs(reducedODs, model.getOutputDirectory());
			
			printTimeStamp();
			System.out.println("Done");
			//TODO eventually, we'll do multiple instances of traffic assignment here instead of just writing to files
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(2);
		}
	}

	/**
	 * This method writes the current amount of milliseconds that have elapsed since the start of the script
	 */
	private static void printTimeStamp() {
		System.out.print((System.currentTimeMillis()-wrapNCTCOG.startMS)+" ms\t");
	}
	

	/**
	 * @return a Stream consisting of the TripPurposes which should be used for this model's primary trips
	 */
	private static Stream<TripPurpose> getPrimaryTripPurposes() {
		return Stream.of(
				TripPurpose.HOME_WORK,
				TripPurpose.HOME_SHOP,
				TripPurpose.HOME_SRE,
//				TripPurpose.HOME_K12,
				TripPurpose.HOME_PBO
				).parallel();
	}

	
	/** This method returns a mapping from each trip purpose to the model's production demand rates
	 * @param model the model input whence the rates should be retrieved
	 * @return a map from each trip purpose to its pertinent market segments and their rates
	 */
	private static Map<TripPurpose, Map<MarketSegment, Double>> getProdRates(ModelInput model) {
		return getPrimaryTripPurposes().collect(Collectors.toMap(Function.identity(), //for each trip purpose,
				purpose -> model.getGeneralProdRates(purpose)));	//map the purpose to the model's production rates
	}

	/**This method generates mappings from each trip purpose and market segment to 
	 * a demand map containing the purpose/segment combination's productions
	 * 
	 * @param g the graph whose productions should be generated
	 * @param prodRates the rates by which trips are generated for each trip purpose and market segment
	 * @return a mapping from each purpose to a map from each pertinent market segment to its production demand map
	 */
	private static Map<TripPurpose, Map<MarketSegment, DemandMap>> getProds(Graph g,
			Map<TripPurpose, Map<MarketSegment, Double>> prodRates) {
		
		return prodRates.keySet().parallelStream() //a map from each trip purpose to
				.collect(Collectors.toMap(Function.identity(), 
						//the corresponding market segment-level mapping
						purpose -> generateProductions(g, prodRates.get(purpose))));
	}
		
	/** This method generates trips for a given collection of MarketSegments
	 * @param g the graph on which trips should be generated
	 * @param prodRates the rates at which trips are generated
	 * @return a Map from each MarketSegment to a production DemandMap 
	 */
	private static Map<MarketSegment, DemandMap> generateProductions(Graph g, Map<MarketSegment, Double> prodRates) {
		//initalize a basic trip generator
		BasicTripGenerator generator = new BasicTripGenerator(g,prodRates);	
		//feed each MarketSegment's production rate into the generator and collect their resulting DemandMaps
		return prodRates.keySet().parallelStream().collect(Collectors.toMap(Function.identity(), seg ->  generator.generate(seg)));
	}

	
	/** This method returns a mapping from each trip purpose to the model's attraction demand rates
	 * @param model the model input whence the rates should be retrieved
	 * @return a map from each trip purpose to its pertinent market segments and their rates
	 */
	private static Map<TripPurpose, Map<MarketSegment, Map<AreaClass, Double>>> getAttrRates(ModelInput model) {
		
		return getPrimaryTripPurposes()	//for each trip purpose, map the purpose to the model's attraction rates
				.collect(Collectors.toMap(Function.identity(), purpose -> model.getAreaClassAttrRates(purpose)));
	}	

	/** This method generates mappings from each trip purpose and market segment to 
	 * a demandmap containing the purpose/segment combination's attractions
	 * 
	 * @param g the graph whose attractions should be generated
	 * @param attrRates the rates by which trips are generated for each trip purpose, market segment, and area class
	 * @return a mapping from each purpose to a map from each pertinent market segment to its attraction demand map
	 */
	private static Map<TripPurpose, Map<MarketSegment, DemandMap>> getAttrs(Graph g,
			Map<TripPurpose, Map<MarketSegment, Map<AreaClass, Double>>> attrRates) {
		
		return attrRates.keySet().parallelStream()	//a map from each trip purpose to
				.collect(Collectors.toMap(Function.identity(), 
						//the corresponding market segment-level mapping
						purpose -> generateAttractions(g, attrRates.get(purpose))));
	}
	
	/** This method generates trips for a given collection of MarketSegments
	 * @param g the graph on which trips should be generated
	 * @param attrRates the rates at which trips are generated for each area class
	 * @return a Map from each MarketSegment to an attraction DemandMap
	 */
	private static Map<MarketSegment, DemandMap> generateAttractions(Graph g, Map<MarketSegment, Map<AreaClass, Double>> attrRates) {
		AreaSpecificTripGenerator generator = new AreaSpecificTripGenerator(g,attrRates);
		return attrRates.keySet().parallelStream().collect(Collectors.toMap(Function.identity(), seg -> generator.generate(seg)));
	}

	/** This method transfers combined (i.e. read-obly) PAMaps' data into a writable array-backed PAMap
	 * @param maps the maps that should be flattened, as a mapping from each trip purpose to each marketsegment to each map
	 * @return the same mapping with PAMaps replaced by duplicate writable instances
	 */
	private static Map<TripPurpose,Map<MarketSegment,PAMap>> flatten(Map<TripPurpose,Map<MarketSegment,PAMap>> maps) {
		
		return maps.entrySet().parallelStream().collect(					//for each trip purpose,
				Collectors.toMap(Entry::getKey, purposeEntry ->				//map the purpose to
				
			purposeEntry.getValue().entrySet().parallelStream().collect(	//a map for each marketsegment of this purpose
					Collectors.toMap(Entry::getKey, segmentEntry->			//where the segment maps to
					
				new FixedSizePAMap(segmentEntry.getValue()) 			//a FixedSize duplicate of the original map 
			))
		));
	}
	
	private static Map<TripPurpose, Map<MarketSegment, PAMap>> combine(Graph g,
			Map<TripPurpose, Map<MarketSegment, DemandMap>> primaryProds,
			Map<TripPurpose, Map<MarketSegment, DemandMap>> primaryAttrs) {
		
		return primaryProds.entrySet().parallelStream().collect(	//For each trip purpose,
				Collectors.toMap(Entry::getKey, 	//return a map from the purpose to 
						
						purposeEntry -> Stream.concat(		//a mapping combination
								//across all marketsegments for this purpose
								purposeEntry.getValue().keySet().parallelStream(),
								primaryAttrs.get(purposeEntry.getKey()).keySet().parallelStream()
								).distinct()	//(without duplicates)
						.collect(Collectors.toMap(Function.identity(),	//where each marketsegment is mapped to 
								
								//a PAPassthroughMap which incorporates the production and attraction demandmaps into a single object
								segment -> new PAPassthroughMap(g,
										purposeEntry.getValue().get(segment), 
										primaryAttrs.get(purposeEntry.getKey()).get(segment))))));
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
		
		Map<TripPurpose,TripPurpose> purposePairs = new HashMap<TripPurpose,TripPurpose>(3);
		purposePairs.put(TripPurpose.HOME_WORK, TripPurpose.NONHOME_WORK);
		purposePairs.put(TripPurpose.HOME_NONWORK, TripPurpose.NONHOME_NONWORK);
		
		Map<TripPurpose,Map<Mode,Map<Boolean,Double>>> vots = new HashMap<TripPurpose,Map<Mode,Map<Boolean,Double>>>();
		vots.put(TripPurpose.HOME_WORK, new HashMap<Mode,Map<Boolean,Double>>());
		vots.put(TripPurpose.HOME_NONWORK, new HashMap<Mode,Map<Boolean,Double>>());
		
		vots.get(TripPurpose.HOME_WORK).put(Mode.SINGLE_OCC, new HashMap<Boolean,Double>());
		vots.get(TripPurpose.HOME_WORK).put(Mode.HOV, new HashMap<Boolean,Double>());
		vots.get(TripPurpose.HOME_NONWORK).put(Mode.SINGLE_OCC, new HashMap<Boolean,Double>());
		vots.get(TripPurpose.HOME_NONWORK).put(Mode.HOV, new HashMap<Boolean,Double>());
		
		vots.get(TripPurpose.HOME_WORK).get(Mode.SINGLE_OCC).put(false,0.35);
		vots.get(TripPurpose.HOME_WORK).get(Mode.SINGLE_OCC).put(true,0.9);
		vots.get(TripPurpose.HOME_WORK).get(Mode.HOV).put(false,0.35);
		vots.get(TripPurpose.HOME_WORK).get(Mode.HOV).put(true,0.9);
		vots.get(TripPurpose.HOME_NONWORK).get(Mode.SINGLE_OCC).put(false,0.17);
		vots.get(TripPurpose.HOME_NONWORK).get(Mode.SINGLE_OCC).put(true,0.45);
		vots.get(TripPurpose.HOME_NONWORK).get(Mode.HOV).put(false,0.17);
		vots.get(TripPurpose.HOME_NONWORK).get(Mode.HOV).put(true,0.45);
		
		
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
						.collect(new ODMatrixCollector(mode,vots.get(tripPurpose).get(mode).get(false)))
					);
			
					//Next, handle the Work IG4 and Nonwork cases
					ret.add(
							Stream.concat(	//Combine two types of maps
									hbODs.get(timePeriod)	//home-based trips from this time period
									.get(tripPurpose).entrySet().parallelStream()	//for this trip purpose
									.filter(entry -> entry.getKey() instanceof IncomeGroupSegmenter && ((IncomeGroupSegmenter) entry.getKey()).getIncomeGroup() >= 4)	//in income group 4
									.map(Entry::getValue).flatMap(Collection::parallelStream).filter(od -> od.getMode() == mode || (mode == Mode.HOV && (od.getMode() == Mode.HOV_2_PSGR || od.getMode() == Mode.HOV_3_PSGR))),	//for this mode
									
									nhbODs.get(timePeriod)	//And non-home-based trips
									.get(purposePairs.get(tripPurpose)).parallelStream()	//for this trip purpose
									.filter(od -> od.getMode() == mode)	//using this mode
									)
							.collect(new ODMatrixCollector(mode,vots.get(tripPurpose).get(mode).get(true)))
					);
				})
			);
			
			return ret;
		}));
	}
	
	
	private static void writeODs(Map<TimePeriod, Collection<ODMatrix>> ods, String outputDir) {
		
		ods.entrySet().parallelStream()
		.forEach(todEntry -> 
			todEntry.getValue().parallelStream()
			.forEach(matrix -> ODMatrixBINWriter.write(outputDir,todEntry.getKey(), matrix)));
	}
	
}
