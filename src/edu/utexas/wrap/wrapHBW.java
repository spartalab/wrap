package edu.utexas.wrap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
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
import edu.utexas.wrap.marketsegmentation.IncomeGroupSegment;
import edu.utexas.wrap.marketsegmentation.IncomeGroupIndustrySegment;
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
			//Model inputs
			File graphFile = new File(args[1]);
			Collection<MarketSegment> prodSegs = IntStream.range(1,4).parallel().mapToObj(ig -> new IncomeGroupSegment(ig)).collect(Collectors.toSet()),
					attrSegs = Stream.of(IndustryClass.values()).parallel().flatMap(ic ->
						IntStream.range(1, 4).parallel().mapToObj(ig -> new IncomeGroupIndustrySegment(ig, ic))
					).collect(Collectors.toSet());
			
			Map<TimePeriod,Double> depRates = null, //TODFactors.csv
								   arrRates = null; //TODFactors.csv

			Map<MarketSegment,Map<Mode,Double>> modeShares = null; // ModeChoiceSplits.xlsx
			Map<Mode,Double> occRates = null; // modalOccRates.csv

			//TODO need to add command line argument for the prodRates
			Map<MarketSegment,Double> vots = VOTFactory.readVOTFile(), //TODO Don't have file yet
					 				  prodRates = ProductionAttractionFactory.readProductionRates(), //TripAttRates.csv
									  pkRates = PeakFactory.readPkOPkSplitRates(); // pkOffPkSplits.csv

			Map<MarketSegment,Map<AreaClass,Double>> attrRates = ProductionAttractionFactory.readAttractionRates(); //TripProdRates.csv

			//TODO find skim file
			FrictionFactorMap ffm = FrictionFactorFactory.readFactorFile(new File("../../nctcogFiles/FFactorHBW_INC1 OP.csv"),true,null); //Have file but don't know which income groups to use
			
			Map<TimePeriod,Path> outputODPaths = null;

			
			Graph g = GraphFactory.readEnhancedGraph(graphFile, 50000);
			//TODO read RAAs
			//TODO add demographic data to zones
			
			//Perform trip generation
			Map<MarketSegment, PAMap> maps = tripGenerator(g, prodSegs, attrSegs, vots, prodRates, attrRates);
			
			//Perform trip balancing
			balance(g, maps);
						
			Map<MarketSegment,Map<TimePeriod,PAMap>> timeMaps = pkOpSplitting(maps,pkRates);
			
			//Perform trip distribution
			Map<MarketSegment, Map<TimePeriod, AggregatePAMatrix>> aggMtxs = tripDistribution(ffm, g, timeMaps);
			
			
			Map<MarketSegment,AggregatePAMatrix> aggCombinedMtxs = aggMtxs.entrySet().parallelStream()
					.collect(Collectors.toMap(Entry::getKey, entry -> Combiner.combineAggregateMatrices(g, entry.getValue().values())));
						
			//Perform mode choice splitting
			Map<MarketSegment, ModalPAMatrix> modalMtxs = modeChoice(modeShares, aggCombinedMtxs);

			//PA to OD splitting by time of day
			Map<TimePeriod, ODMatrix> ods = paToODConversion(modalMtxs, depRates, arrRates, occRates);
			
			
			//Write to file AM and PM peak OD matrices
			ods.entrySet().parallelStream()
			.filter(entry -> 
				entry.getKey().equals(TimePeriod.MORN_PK) || 
				entry.getKey().equals(TimePeriod.AFTERNOON_PK))
			.forEach(entry -> entry.getValue().write(outputODPaths.get(entry.getKey())));
			
			//Combine off-peak matrices and output to file
//			Path outputFile = null;
//			ODMatrix offPeak = Combiner.combineODMatrices(
//					ods.entrySet().parallelStream()
//					.filter(entry -> 
//						!entry.getKey().equals(TimePeriod.MORN_PK) && 
//						!entry.getKey().equals(TimePeriod.AFTERNOON_PK))
//					.map(entry -> entry.getValue()));
//			offPeak.write(outputFile);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(2);
		}
	}



	private static Map<MarketSegment, PAMap> tripGenerator(Graph g, 
			Collection<MarketSegment> prodSegs, Collection<MarketSegment> attrSegs,
			Map<MarketSegment, Double> vots, Map<MarketSegment, Double> prodRates,
			Map<MarketSegment, Map<AreaClass, Double>> attrRates) {
		
		BasicTripGenerator prodGenerator = new BasicTripGenerator(g,prodRates);
		AreaSpecificTripGenerator attrGenerator = new AreaSpecificTripGenerator(g,attrRates);

		Map<MarketSegment,Map<TravelSurveyZone,Double>> prods = prodSegs.parallelStream().collect(Collectors.toMap(Function.identity(), seg -> prodGenerator.generate(seg)));
		Map<MarketSegment,Map<TravelSurveyZone,Double>> attrs = attrSegs.parallelStream().collect(Collectors.toMap(Function.identity(), seg -> attrGenerator.generate(seg)));
		
		return prodSegs.parallelStream().collect(Collectors.toMap(Function.identity(), 
				seg -> new PAPassthroughMap(g, vots.get(seg), prods.get(seg),attrs.get(seg))));
	}

	private static void balance(Graph g, Map<MarketSegment, PAMap> timeMaps) {
		Prod2AttrProportionalBalancer balancer = new Prod2AttrProportionalBalancer(g.getRAAs());
		timeMaps.values().parallelStream().forEach(map -> balancer.balance(map));
	}
	
	private static Map<MarketSegment, Map<TimePeriod, PAMap>> pkOpSplitting(Map<MarketSegment, PAMap> maps,
			Map<MarketSegment, Double> pkRates) {
		// TODO Auto-generated method stub
		return maps.entrySet().parallelStream().collect(Collectors.toMap(Entry::getKey, 
				entry -> {
					double pkRate = pkRates.get(entry.getKey());
					PAMap pkMap = new FixedMultiplierPassthroughPAMap(entry.getValue(),pkRate);
					PAMap opMap = new FixedMultiplierPassthroughPAMap(entry.getValue(),1-pkRate);
					Map<TimePeriod,PAMap> ret = new HashMap<TimePeriod,PAMap>(3,1.0f);
					ret.put(TimePeriod.MORN_PK, pkMap);
					ret.put(TimePeriod.EARLY_OFFPK, opMap);
					return ret;
				}));
	}
	private static Map<MarketSegment, Map<TimePeriod,AggregatePAMatrix>> tripDistribution(FrictionFactorMap ffm, Graph g,
			Map<MarketSegment, Map<TimePeriod, PAMap>> timeMaps) {
		TripDistributor distributor = new GravityDistributor(g,ffm);
		timeMaps.entrySet().parallelStream().collect(Collectors.toMap(Entry::getKey, entry -> 
			entry.getValue().entrySet().parallelStream()
			.collect(Collectors.toMap(Function.identity(), inner -> distributor.distribute(inner.getValue())))
		));
		return null;
	}

	private static Map<MarketSegment, ModalPAMatrix> modeChoice(Map<MarketSegment, Map<Mode, Double>> modeShares,
			Map<MarketSegment, AggregatePAMatrix> aggMtxs) {
		TripInterchangeSplitter mc = new FixedProportionSplitter(modeShares);
		return aggMtxs.entrySet().parallelStream()
				.collect(Collectors.toMap(Entry::getKey, 
						entry -> mc.split(entry.getValue(),entry.getKey())
						//FIXME This next line filters out all modes but driving alone
						.filter(mtx -> mtx.getMode().equals(Mode.SINGLE_OCC)).findFirst().get()));
	}
	
	private static Map<TimePeriod, ODMatrix> paToODConversion(
			Map<MarketSegment, ModalPAMatrix> modalMtxs,
			Map<TimePeriod, Double> departureRates, 
			Map<TimePeriod, Double> arrivalRates, 
			Map<Mode, Double> occupancyRates) {
		
		return Stream.of(TimePeriod.values()).parallel()			
		.collect(Collectors.toMap(Function.identity(), tp -> {
			
			//Convert using TOD splitting
			DepartureArrivalConverter converter = new DepartureArrivalConverter(departureRates.get(tp),arrivalRates.get(tp));
			Stream<ODMatrix> tpODs = modalMtxs.values().parallelStream()
					.map(entry -> converter.convert(entry, occupancyRates.get(entry.getMode())));
			
			//Combine across income groups 1,2,3 and vehicle ownership
			return Combiner.combineODMatrices(tpODs);

		}));
	}
}
