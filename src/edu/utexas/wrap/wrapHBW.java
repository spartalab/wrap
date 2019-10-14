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
import edu.utexas.wrap.demand.containers.PAPassthroughMap;
import edu.utexas.wrap.distribution.FrictionFactorMap;
import edu.utexas.wrap.distribution.GravityDistributor;
import edu.utexas.wrap.distribution.TripDistributor;
import edu.utexas.wrap.generation.AreaSpecificTripGenerator;
import edu.utexas.wrap.generation.BasicTripGenerator;
import edu.utexas.wrap.generation.RateProportionTripGenerator;
import edu.utexas.wrap.marketsegmentation.IncomeGroupSegment;
import edu.utexas.wrap.marketsegmentation.IncomeGroupSegmenter;
import edu.utexas.wrap.marketsegmentation.IncomeGroupIndustrySegment;
import edu.utexas.wrap.marketsegmentation.IncomeGroupWorkerVehicleSegment;
import edu.utexas.wrap.marketsegmentation.IndustryClass;
import edu.utexas.wrap.marketsegmentation.MarketSegment;
import edu.utexas.wrap.modechoice.FixedProportionSplitter;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.modechoice.TripInterchangeSplitter;
import edu.utexas.wrap.net.AreaClass;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;
import edu.utexas.wrap.util.*;

public class wrapHBW {

	public static void main(String[] args) {
		try{
			System.out.print("Reading network... ");
			long ms = System.currentTimeMillis();
			//Model inputs
			Graph graph = GraphFactory.readEnhancedGraph(new File(args[0]),Integer.parseInt(args[1]));
			long nms = System.currentTimeMillis();
			System.out.println(""+(nms-ms)/1000.0+" s");
			
			//Market segmentation
			Collection<MarketSegment> segmentsAfterGeneration = IntStream.range(1,4).parallel().mapToObj(ig -> new IncomeGroupSegment(ig)).collect(Collectors.toSet());



			//TODO need to add command line argument for the prodRates
			
			System.out.print("Reading modal shares and occupancy rates... ");
			//Mode choice inputs
			ms = System.currentTimeMillis();
			Map<Mode,Double> occRates = ModeFactory.readOccRates(new File("../../nctcogFiles/modalOccRates.csv"), true); // modalOccRates.csv
			nms = System.currentTimeMillis();
			System.out.println(""+(nms-ms)/1000.0+" s");
			
			
			System.out.print("Reading time-of-day rates... ");
			//TOD splitting inputs
			ms = System.currentTimeMillis();
			Map<MarketSegment, Map<TimePeriod,Double>> depRates = TimePeriodRatesFactory.readDepartureFile(new File("../../nctcogFiles/TODfactors.csv"), segmentsAfterGeneration), //TODFactors.csv
					   arrRates = TimePeriodRatesFactory.readArrivalFile(new File("../../nctcogFiles/TODfactors.csv"), segmentsAfterGeneration); //TODFactors.csv
			nms = System.currentTimeMillis();
			System.out.println(""+(nms-ms)/1000.0+" s");
			
			//TODO determine output files
			Map<TimePeriod,Path> outputODPaths = new HashMap<TimePeriod,Path>();
			outputODPaths.put(TimePeriod.AM_PK, Paths.get("morningPeak.csv"));
			outputODPaths.put(TimePeriod.PM_PK, Paths.get("eveningPeak.csv"));

			
			
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
			
			
			//Here's where the actual model starts
			
			
			System.out.print("Performing trip generation... ");
			//Perform trip generation
			ms = System.currentTimeMillis();
			Map<MarketSegment, PAMap> hbMaps = tripGenerator(graph, segmentsAfterGeneration);
			nms = System.currentTimeMillis();
			System.out.println(""+(nms-ms)/1000.0+" s");

			
			
			System.out.print("Performing trip balancing... ");
			//Perform trip balancing
			ms = System.currentTimeMillis();
			balance(graph, hbMaps);
			nms = System.currentTimeMillis();
			System.out.println(""+(nms-ms)/1000.0+" s");
			System.out.println("MSE Balanced Production Error " + MSEErrorCalculator.measureTripGenProdError(graph, hbMaps,"../../nctcogFiles/gen-dist/tripsgen_balanced_HBW.csv", segmentsAfterGeneration, true));
			System.out.println("MSE Balanced Attraction Error " + MSEErrorCalculator.measureTripGenAttrError(graph, hbMaps,"../../nctcogFiles/gen-dist/tripsgen_balanced_HBW.csv", segmentsAfterGeneration, true));
			System.out.println("R^2 Balanced Production Error " + R2ErrorCalculator.measureTripGenProdError(graph, hbMaps,"../../nctcogFiles/gen-dist/tripsgen_balanced_HBW.csv", segmentsAfterGeneration, true));
			System.out.println("R^2 Balanced Attraction Error " + R2ErrorCalculator.measureTripGenAttrError(graph, hbMaps,"../../nctcogFiles/gen-dist/tripsgen_balanced_HBW.csv", segmentsAfterGeneration, true));
			
			System.out.print("Performing peak-offpeak splitting... ");
			ms = System.currentTimeMillis();
			Map<MarketSegment,Map<TimePeriod,PAMap>> timeMaps = pkOpSplitting(hbMaps,segmentsAfterGeneration);
			nms = System.currentTimeMillis();
			System.out.println(""+(nms-ms)/1000.0+" s");
			
			
			System.out.print("Performing trip distribution... ");
			//Perform trip distribution
			ms = System.currentTimeMillis();
			Map<MarketSegment, Map<TimePeriod, AggregatePAMatrix>> aggMtxs = tripDistribution(segmentsAfterGeneration, graph, timeMaps);
			nms = System.currentTimeMillis();
			System.out.println(""+(nms-ms)/1000.0+" s");
			
			
			System.out.print("Performing matrix aggregation... ");
			//FIXME study how this should actually behave - PK and OP splitting doesn't just get combined back together
			ms = System.currentTimeMillis();
			Map<MarketSegment,AggregatePAMatrix> aggCombinedMtxs = aggMtxs.entrySet().parallelStream()
					.collect(Collectors.toMap(Entry::getKey, entry -> Combiner.combineAggregateMatrices(graph, entry.getValue().values())));
			nms = System.currentTimeMillis();
			System.out.println(""+(nms-ms)/1000.0+" s");
			
			
			System.out.print("Performing mode choice splitting... ");
			//Perform mode choice splitting
			ms = System.currentTimeMillis();
			Map<MarketSegment, Collection<ModalPAMatrix>> modalMtxs = modeChoice(segmentsAfterGeneration, aggCombinedMtxs);
			nms = System.currentTimeMillis();
			System.out.println(""+(nms-ms)/1000.0+" s");
			
			
			System.out.print("Performing PA-to-OD matrix conversion... ");
			//PA to OD splitting by time of day
			ms = System.currentTimeMillis();
			Map<TimePeriod, Map<Mode, ODMatrix>> ods = paToODConversion(modalMtxs, depRates, arrRates, occRates);
			nms = System.currentTimeMillis();
			System.out.println(""+(nms-ms)/1000.0+" s");
			
			
			System.out.print("Writing OD matrices... ");
			//Write to file AM and PM peak OD matrices
			ms = System.currentTimeMillis();
//			ods.entrySet().parallelStream()
//			.filter(entry -> 
////				entry.getKey().equals(TimePeriod.AM_PK) || 
//				entry.getKey().equals(TimePeriod.PM_PK))
//			.forEach(entry -> entry.getValue().write(outputODPaths.get(entry.getKey())));
			nms = System.currentTimeMillis();
			System.out.println(""+(nms-ms)/1000.0+" s");
			
			//Combine off-peak matrices and output to file
//			Path outputFile = null;
//			ODMatrix offPeak = Combiner.combineODMatrices(
//					ods.entrySet().parallelStream()
//					.filter(entry -> 
//						!entry.getKey().equals(TimePeriod.MORN_PK) && 
//						!entry.getKey().equals(TimePeriod.AFTERNOON_PK))
//					.map(entry -> entry.getValue()));
//			offPeak.write(outputFile);
			
			System.out.println("Completed successfully");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(2);
		}
	}



	private static Map<MarketSegment, FrictionFactorMap> readFrictionFactorMaps(
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

	private static Map<MarketSegment, PAMap> tripGenerator(Graph g,
			Collection<MarketSegment> afterProdSegs) throws IOException {

		//Production segmentation
		Collection<MarketSegment> prodSegs = IntStream.range(1, 4).parallel().boxed().flatMap(incomeGroup -> 
			IntStream.range(0, 4).parallel().boxed().flatMap(numberOfWorkers ->
				IntStream.range(0,4).parallel().boxed().map(numberOfVehicles ->
				new IncomeGroupWorkerVehicleSegment(incomeGroup, numberOfWorkers, numberOfVehicles)
					)
				)
			).collect(Collectors.toSet());
		
		//Read segments' production rates
		Map<MarketSegment,Double> primaryProdRates = ProductionAttractionFactory.readProductionRates(new File("../../nctcogFiles/TripProdRates.csv"), true, false,prodSegs); //TripAttRates.csv
		Map<MarketSegment,Double> secondaryProdRates = ProductionAttractionFactory.readProductionRates(null,true,false,prodSegs);	//FIXME no file yet	
		//Generate primary productions
		Map<MarketSegment, Map<TravelSurveyZone, Double>> primaryProds = generatePrimaryProductions(g, prodSegs, primaryProdRates);
		//Generate secondary productions
		Map<MarketSegment, Map<TravelSurveyZone, Double>> secondaryProds = generateSecondaryProductions(g, primaryProdRates, primaryProds, secondaryProdRates);		
		
//		System.out.println("MSE Production Error " + MSEErrorCalculator.measureTripGenSegmentedProdError(graph, hbMaps,"../../nctcogFiles/gen-dist/TSZtrips_pro_hbw.csv", productionSegments, afterPASegments));
//		System.out.println("MSE Attraction Error " + MSEErrorCalculator.measureTripGenAttrError(graph, hbMaps,"../../nctcogFiles/gen-dist/TSZtrips_att_hbw.csv", afterPASegments, false));
//		System.out.println("R^2 Production Error " + R2ErrorCalculator.measureTripGenSegmentedProdError(graph, hbMaps,"../../nctcogFiles/gen-dist/TSZtrips_pro_hbw.csv", productionSegments, afterPASegments));
//		System.out.println("R^2 Attraction Error " + R2ErrorCalculator.measureTripGenAttrError(graph, hbMaps,"../../nctcogFiles/gen-dist/TSZtrips_att_hbw.csv", afterPASegments, false));
		
		
		
		//Attraction segmentation
		Collection<MarketSegment> attrSegs = Stream.of(IndustryClass.values()).parallel().flatMap(ic ->
		IntStream.range(1, 4).parallel().mapToObj(ig -> new IncomeGroupIndustrySegment(ig, ic))
			).collect(Collectors.toSet());
		//Read segments' attraction rates
		Map<MarketSegment,Map<AreaClass,Double>> primaryAttrRates = ProductionAttractionFactory.readAttractionRates(new File("../../nctcogFiles/TripAttRates.csv"), true, attrSegs); //TripProdRates.csv
		//Generate primary attractions
		Map<MarketSegment, Map<TravelSurveyZone, Double>> primaryAttrs = generatePrimaryAttractions(g, attrSegs, primaryAttrRates);


		Map<MarketSegment,Map<TravelSurveyZone,Double>> combinedPrimaryProds = combineSegmentsByIncomeGroup(afterProdSegs, primaryProds);
		
		Map<MarketSegment,Map<TravelSurveyZone,Double>> combinedPrimaryAttrs = combineSegmentsByIncomeGroup(afterProdSegs, primaryAttrs);
		
		Map<MarketSegment,Map<TravelSurveyZone,Double>> combinedSecondaryProds = combineSegmentsByIncomeGroup(afterProdSegs, secondaryProds);
		
		
		
		
		
		return afterProdSegs.parallelStream().collect(Collectors.toMap(Function.identity(),
				seg -> new PAPassthroughMap(g, null, combinedPrimaryProds.get(seg), combinedPrimaryAttrs.get(seg))));
	}



	private static Map<MarketSegment, Map<TravelSurveyZone, Double>> combineSegmentsByIncomeGroup(
			Collection<MarketSegment> afterProdSegs, Map<MarketSegment, Map<TravelSurveyZone, Double>> primaryProds) {
		return afterProdSegs.parallelStream().collect(Collectors.toMap(Function.identity(), newSeg ->
		primaryProds.entrySet().parallelStream()
		.filter(entry -> entry.getKey() instanceof IncomeGroupSegmenter && ((IncomeGroupSegmenter) entry.getKey()).getIncomeGroup() == ((IncomeGroupSegmenter) newSeg).getIncomeGroup())
		.flatMap(entry -> entry.getValue().entrySet().parallelStream())
		.collect(Collectors.toConcurrentMap(Entry::getKey, Entry::getValue, Double::sum))));
	}



	private static Map<MarketSegment, Map<TravelSurveyZone, Double>> generatePrimaryAttractions(Graph g,
			Collection<MarketSegment> attrSegs, Map<MarketSegment, Map<AreaClass, Double>> primaryAttrRates) {
		AreaSpecificTripGenerator primaryAttrGenerator = new AreaSpecificTripGenerator(g,primaryAttrRates);
		Map<MarketSegment,Map<TravelSurveyZone,Double>> primaryAttrs = attrSegs.parallelStream().collect(Collectors.toMap(Function.identity(), seg -> primaryAttrGenerator.generate(seg)));
		return primaryAttrs;
	}



	private static Map<MarketSegment, Map<TravelSurveyZone, Double>> generateSecondaryProductions(Graph g,
			Map<MarketSegment, Double> primaryProdRates, Map<MarketSegment, Map<TravelSurveyZone, Double>> primaryProds,
			Map<MarketSegment, Double> secondaryProdRates) {
		RateProportionTripGenerator secondaryProdGenerator = new RateProportionTripGenerator(g, primaryProdRates, secondaryProdRates, primaryProds);

		Map<MarketSegment,Map<TravelSurveyZone,Double>> secondaryProds = primaryProds.entrySet().parallelStream().collect(Collectors.toMap(
				Entry::getKey, entry -> secondaryProdGenerator.generate(primaryProds.get(entry.getKey()), entry.getKey())));
		return secondaryProds;
	}



	private static Map<MarketSegment, Map<TravelSurveyZone, Double>> generatePrimaryProductions(Graph g,
			Collection<MarketSegment> prodSegs, Map<MarketSegment, Double> primaryProdRates) {
		BasicTripGenerator primaryProdGenerator = new BasicTripGenerator(g,primaryProdRates);
		Map<MarketSegment,Map<TravelSurveyZone,Double>> primaryProds = prodSegs.parallelStream().collect(Collectors.toMap(Function.identity(), seg ->  primaryProdGenerator.generate(seg)));
		return primaryProds;
	}

	private static void balance(Graph g, Map<MarketSegment, PAMap> timeMaps) {
		Prod2AttrProportionalBalancer balancer = new Prod2AttrProportionalBalancer(null);
		timeMaps.values().parallelStream().forEach(map -> balancer.balance(map));
	}

	private static Map<MarketSegment, Map<TimePeriod, PAMap>> pkOpSplitting(Map<MarketSegment, PAMap> maps,
			Collection<MarketSegment> segments) throws IOException {
		Map<MarketSegment,Double> pkRates = PeakFactory.readPkOPkSplitRates(new File("../../nctcogFiles/pkOffPkSplits.csv"), true, segments); // pkOffPkSplits.csv
		// TODO Auto-generated method stub
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
	private static Map<MarketSegment, Map<TimePeriod,AggregatePAMatrix>> tripDistribution(Collection<MarketSegment> segments, Graph g,
			Map<MarketSegment, Map<TimePeriod, PAMap>> timeMaps) throws IOException {
		
		System.out.print("Reading friction factor maps... ");
		//Create FF Maps for each segment
		double ms = System.currentTimeMillis();
		Map<MarketSegment, FrictionFactorMap> ffm = readFrictionFactorMaps(segments, g);
		double nms = System.currentTimeMillis();
		
		System.out.println(""+(nms-ms)/1000.0+" s");
		return timeMaps.entrySet().parallelStream().collect(Collectors.toMap(Entry::getKey, entry -> 
			entry.getValue().entrySet().parallelStream()
			.collect(Collectors.toMap(Entry::getKey, inner -> {
				TripDistributor distributor = new GravityDistributor(g,ffm.get(entry.getKey()));
				return distributor.distribute(inner.getValue());
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
	
	private static Map<TimePeriod, Map<Mode,ODMatrix>> paToODConversion(
			Map<MarketSegment, Collection<ModalPAMatrix>> modalMtxs,
			Map<MarketSegment,Map<TimePeriod, Double>> departureRates, 
			Map<MarketSegment,Map<TimePeriod, Double>> arrivalRates, 
			Map<Mode, Double> occupancyRates) {
		

		
		return Stream.of(TimePeriod.values()).parallel()			
		.collect(Collectors.toMap(Function.identity(), tp -> {
			
			//Convert using TOD splitting
			Stream<ODMatrix> tpODs = modalMtxs.entrySet().parallelStream()
					.flatMap(entry -> {
						DepartureArrivalConverter converter = new DepartureArrivalConverter(departureRates.get(entry.getKey()).get(tp),arrivalRates.get(entry.getKey()).get(tp));
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
