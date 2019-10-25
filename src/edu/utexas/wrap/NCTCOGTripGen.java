package edu.utexas.wrap;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.demand.containers.PAPassthroughMap;
import edu.utexas.wrap.generation.AreaSpecificTripGenerator;
import edu.utexas.wrap.generation.BasicTripGenerator;
import edu.utexas.wrap.generation.RateProportionTripGenerator;
import edu.utexas.wrap.marketsegmentation.IncomeGroupIndustrySegment;
import edu.utexas.wrap.marketsegmentation.IncomeGroupSegment;
import edu.utexas.wrap.marketsegmentation.IncomeGroupSegmenter;
import edu.utexas.wrap.marketsegmentation.IncomeGroupWorkerVehicleSegment;
import edu.utexas.wrap.marketsegmentation.IndustryClass;
import edu.utexas.wrap.marketsegmentation.IndustrySegment;
import edu.utexas.wrap.marketsegmentation.MarketSegment;
import edu.utexas.wrap.net.AreaClass;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.util.DemandMapCollector;
import edu.utexas.wrap.util.PAMapCollector;
import edu.utexas.wrap.util.ProductionAttractionFactory;

public class NCTCOGTripGen {

	static Map<MarketSegment, PAMap> tripGenerator(Graph g) throws IOException {
		System.out.print("Performing trip generation... ");
		long ms = System.currentTimeMillis();

		//Production segmentation
		Collection<MarketSegment> 
		primaryProdSegs = IntStream.range(1, 5).parallel().boxed().flatMap(incomeGroup -> 
			IntStream.range(0, 4).parallel().boxed().flatMap(numberOfWorkers ->
				IntStream.range(0,4).parallel().boxed().map(numberOfVehicles ->
				new IncomeGroupWorkerVehicleSegment(incomeGroup, numberOfWorkers, numberOfVehicles)
					)
				)
			).collect(Collectors.toSet()),
		
		//Attraction segmentation
		primaryAttrSegs = Stream.of(IndustryClass.values()).parallel().flatMap(ic ->
			IntStream.range(1, 5).parallel().mapToObj(ig -> new IncomeGroupIndustrySegment(ig, ic))
				).collect(Collectors.toSet()),
		secondarySegs = Stream.of(IndustryClass.values()).parallel().map(ic -> new IndustrySegment(ic)).collect(Collectors.toSet()),
		
		//Income group segmentation
		igSegs = IntStream.range(1, 5).parallel().boxed().map(ig -> new IncomeGroupSegment(ig)).collect(Collectors.toSet());
		
		//Read segments' production rates
		Map<MarketSegment,Double> primaryProdRates = ProductionAttractionFactory.readSegmentRates(new File("../../nctcogFiles/TripProdRates.csv"), true, false,primaryProdSegs); //TripAttRates.csv
		Map<MarketSegment,Double> secondaryProdRates = ProductionAttractionFactory.readSegmentRates(null,true,false,secondarySegs),	//FIXME no file yet	
		//Read segments' attraction rates
		secondaryWrkAttrRates = ProductionAttractionFactory.readSegmentRates(null, true, false, secondarySegs), //FIXME
		secondaryESHAttrRates = ProductionAttractionFactory.readSegmentRates(null, true, false, secondarySegs),
		secondaryOthAttrRates = ProductionAttractionFactory.readSegmentRates(null, true, false, secondarySegs);
		Map<MarketSegment,Map<AreaClass,Double>> primaryAttrRates = ProductionAttractionFactory.readSegmentAreaRates(new File("../../nctcogFiles/TripAttRates.csv"), true, primaryAttrSegs); //TripProdRates.csv

		
		//Generate primary productions
		Map<MarketSegment, DemandMap> primaryProds = generatePrimaryProductions(g, primaryProdSegs, primaryProdRates);
		//Generate primary attractions
		Map<MarketSegment, DemandMap> primaryAttrs = generatePrimaryAttractions(g, primaryAttrSegs, primaryAttrRates);

		//Combine primary maps across non-income-group segments
		Map<MarketSegment,DemandMap> combinedPrimaryProds = combineMapsByIncomeGroupSegment(igSegs, primaryProds);
		Map<MarketSegment,DemandMap> combinedPrimaryAttrs = combineMapsByIncomeGroupSegment(igSegs, primaryAttrs);
		
		//Generate secondary productions

		Map<MarketSegment, DemandMap> secondaryProds = generateSecondaryProductions(g, primaryProdRates, combinedPrimaryProds, secondaryProdRates);		

		//Generate secondary attractions
		Map<TripPurpose,Map<MarketSegment,DemandMap>> secondaryAttrMap = new HashMap<TripPurpose,Map<MarketSegment,DemandMap>>();
		secondaryAttrMap.put(TripPurpose.WORK_WORK, generateSecondaryAttractions(g, secondarySegs, secondaryWrkAttrRates)); 
		secondaryAttrMap.put(TripPurpose.WORK_ESH, generateSecondaryAttractions(g, secondarySegs, secondaryESHAttrRates));
		secondaryAttrMap.put(TripPurpose.WORK_OTH, generateSecondaryAttractions(g, secondarySegs, secondaryOthAttrRates));
		
		//Combine secondary maps across trip purposes
		Map<MarketSegment,DemandMap> secondaryAttrs = combineMapsByTripPurpose(secondarySegs, secondaryAttrMap);

		PAMap hbwIG123 = igSegs.parallelStream().filter(seg -> ((IncomeGroupSegment) seg).getIncomeGroup() < 4).map(
				seg -> new PAPassthroughMap(g,null, combinedPrimaryProds.get(seg), combinedPrimaryAttrs.get(seg))).collect(new PAMapCollector());
		
		PAMap hbwIG4 = igSegs.parallelStream().filter(seg -> ((IncomeGroupSegment) seg).getIncomeGroup() == 4).map(seg -> new PAPassthroughMap(g,null,combinedPrimaryProds.get(seg),combinedPrimaryAttrs.get(seg))).findAny().get(),
		nhbw = new PAPassthroughMap(g,null,secondaryProds.values().parallelStream().collect(new DemandMapCollector()), secondaryAttrs.values().parallelStream().collect(new DemandMapCollector())),
		hbwIG4_nhbw = Stream.of(hbwIG4,nhbw).collect(new PAMapCollector());
		
		long nms = System.currentTimeMillis();
		System.out.println(""+(nms-ms)/1000.0+" s");
		
		return null;
	}

	private static Map<MarketSegment, DemandMap> combineMapsByTripPurpose(
			Collection<MarketSegment> secondarySegs, Map<TripPurpose, Map<MarketSegment, DemandMap>> secondaryAttrMap) {
		return secondarySegs.parallelStream().collect(Collectors.toMap(Function.identity(), newSeg -> 
		secondaryAttrMap.values().parallelStream().flatMap(map -> map.entrySet().parallelStream())
		.filter(entry -> entry.getKey() == newSeg).map(Entry::getValue).collect(new DemandMapCollector())));
		// TODO Auto-generated method stub
	}



	/** This method combines a set of MarketSegment-DemandMap pairs together 
	 * based on their income group market segment
	 * @param afterProdSegs the set of income group market segments that should be retained
	 * @param demandMaps the maps from more fine-grained market segmentations to their demand maps
	 * @return a reduced MarketSegment-DemandMap pair where all values have the same income group as the key
	 */
	private static Map<MarketSegment, DemandMap> combineMapsByIncomeGroupSegment(
			Collection<MarketSegment> afterProdSegs, 
			Map<MarketSegment, DemandMap> demandMaps) {
		return afterProdSegs.parallelStream().collect(Collectors.toMap(Function.identity(), newSeg ->
			
			 demandMaps.entrySet().parallelStream()
					.filter(entry -> entry.getKey() instanceof IncomeGroupSegmenter && ((IncomeGroupSegmenter) entry.getKey()).getIncomeGroup() == ((IncomeGroupSegmenter) newSeg).getIncomeGroup())
					.map(Entry::getValue)
					.collect(new DemandMapCollector())
		));
	}

	private static Map<MarketSegment, DemandMap> generatePrimaryAttractions(Graph g,
			Collection<MarketSegment> attrSegs, Map<MarketSegment, Map<AreaClass, Double>> primaryAttrRates) {
		AreaSpecificTripGenerator generator = new AreaSpecificTripGenerator(g,primaryAttrRates);
		return attrSegs.parallelStream().collect(Collectors.toMap(Function.identity(), seg -> generator.generate(seg)));
	}

	private static Map<MarketSegment,DemandMap> generateSecondaryAttractions(Graph g,
			Collection<MarketSegment> attrSegs, Map<MarketSegment,Double> attrRates){
		BasicTripGenerator generator = new BasicTripGenerator(g,attrRates);
		return attrSegs.parallelStream().collect(Collectors.toMap(Function.identity(), seg -> generator.generate(seg)));
	}

	private static Map<MarketSegment, DemandMap> generatePrimaryProductions(Graph g,
			Collection<MarketSegment> prodSegs, Map<MarketSegment, Double> primaryProdRates) {
		BasicTripGenerator generator = new BasicTripGenerator(g,primaryProdRates);
		return prodSegs.parallelStream().collect(Collectors.toMap(Function.identity(), seg ->  generator.generate(seg)));
	}
	
	private static Map<MarketSegment, DemandMap> generateSecondaryProductions(Graph g,
			Map<MarketSegment, Double> primaryProdRates, 
			Map<MarketSegment, DemandMap> primaryProds,
			Map<MarketSegment, Double> secondaryProdRates) {
		RateProportionTripGenerator generator = new RateProportionTripGenerator(g, primaryProdRates, secondaryProdRates, primaryProds);

		return primaryProds.entrySet().parallelStream().collect(Collectors.toMap(
				Entry::getKey, entry -> generator.generate(primaryProds.get(entry.getKey()), entry.getKey())));
	}
}
