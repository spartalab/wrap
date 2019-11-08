package edu.utexas.wrap;

import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.ModalPAMatrix;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.demand.containers.PerProductionZoneMultiplierPassthroughMatrix;
import edu.utexas.wrap.distribution.FrictionFactorMap;
import edu.utexas.wrap.distribution.GravityDistributor;
import edu.utexas.wrap.distribution.TripDistributor;
import edu.utexas.wrap.marketsegmentation.MarketSegment;
import edu.utexas.wrap.modechoice.FixedProportionSplitter;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.modechoice.TripInterchangeSplitter;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;
import edu.utexas.wrap.util.AggregatePAMatrixCollector;
import edu.utexas.wrap.util.DepartureArrivalConverter;
import edu.utexas.wrap.util.io.ModeFactory;
import edu.utexas.wrap.util.io.TimePeriodRatesFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class HBThread extends Thread{
	private Graph graph;
	private Map<TimePeriod, Map<TripPurpose, Map<MarketSegment, Collection<ODMatrix>>>> hbODs;
	private Map<TripPurpose,Map<MarketSegment,PAMap>> hbMaps;
	private Map<MarketSegment, PAMap> pkMaps;
	private Collection<MarketSegment> segments;

	public HBThread(Graph graph, Map<MarketSegment, PAMap> pkMaps, Collection<MarketSegment> segments) {
		this.graph = graph;
		this.pkMaps = pkMaps;
		this.segments = segments;
	}

	public void run() {
		Map<MarketSegment, FrictionFactorMap> pkFFMaps = null; //TODO
		HBTripGen hbTripGen = new HBTripGen(pkFFMaps);
		hbTripGen.start();

		Map<TripPurpose, Map<MarketSegment, FrictionFactorMap>> opFFMaps = null; //TODO
		Map<TripPurpose, Map<MarketSegment, AggregatePAMatrix>> aggOPMtxs = offPeakDistribution(graph, hbMaps, opFFMaps);

		try {
			hbTripGen.join();
		} catch(InterruptedException e) {
			System.out.println("Thread is interrupted.\n");
		}

		//After distributing over different friction factor maps, the HBW trips are stuck back together and SRE & PBO matrices are combined
		Map<TripPurpose, Map<MarketSegment, AggregatePAMatrix>> aggCombinedMtxs = combineAggregateMatrices(hbTripGen.getAggPKMtxs(),aggOPMtxs); //TODO combine SRE/PBO and HBWPK/OP matrices

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
		hbODs = paToODConversion(hbModalMtxs, occupancyRates, depRates, arrRates);

		//thread ends here
	}
	
	public Map<TimePeriod, Map<TripPurpose, Map<MarketSegment, Collection<ODMatrix>>>> getODs(){
		return hbODs;
	}

	private static Map<MarketSegment, AggregatePAMatrix> peakDistribution(
			Graph graph,
			Map<MarketSegment, PAMap> pkMaps,
			Map<MarketSegment, FrictionFactorMap> pkFFMaps) {
		return pkMaps.entrySet().parallelStream().collect(Collectors.toMap(Map.Entry::getKey, entry->{
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

		return hbMaps.entrySet().parallelStream().collect(Collectors.toMap(Map.Entry::getKey, purposeEntry ->
				purposeEntry.getValue().entrySet().parallelStream()
						.collect(Collectors.toMap(Map.Entry::getKey, segmentEntry -> {
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
		return aggCombinedMtxs.entrySet().parallelStream().collect(Collectors.toMap(Map.Entry::getKey, purposeMapEntry-> //for each trip purpose
				purposeMapEntry.getValue().entrySet().parallelStream() //Stream of MarketSegment-Matrix entries
						.map(oldSegmentMap ->	//Take each segment-matrix pair
								workerVehicleRates.get(oldSegmentMap.getKey()).entrySet().parallelStream()		//get a stream of all related segment-rateMap pairs
										.collect(Collectors.toMap(Map.Entry::getKey, 										//map each related (more bespoke) segment to
												rateMap ->  new PerProductionZoneMultiplierPassthroughMatrix(oldSegmentMap.getValue(),rateMap.getValue())))	//a rate multiplier map
						).flatMap(map -> map.entrySet().parallelStream()) 			//unpackage the map to a set of entries
						.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))	//collect all the entries together in a single map
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
								Map.Entry::getKey,Collectors.mapping(						//then map to the values
										Map.Entry::getValue, new AggregatePAMatrixCollector()))));	//and combine together using a collector

		return ret;
	}

	private static Map<TripPurpose,Map<MarketSegment, Collection<ModalPAMatrix>>> modeChoice(
			Map<TripPurpose,Map<MarketSegment, AggregatePAMatrix>> aggMtxs,
			Map<MarketSegment,Map<Mode,Double>> modeShares
	) throws IOException {
		TripInterchangeSplitter mc = new FixedProportionSplitter(modeShares);
		return aggMtxs.entrySet().parallelStream().collect(Collectors.toMap(Map.Entry::getKey, purposeMapEntry ->
				purposeMapEntry.getValue().entrySet().parallelStream()
						.collect(Collectors.toMap(Map.Entry::getKey,
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
				hbModalMtxs.entrySet().parallelStream().collect(Collectors.toMap(Map.Entry::getKey, purposeEntry -> //for each trip purpose
						purposeEntry.getValue().entrySet().parallelStream().collect(Collectors.toMap(Map.Entry::getKey, segmentEntry ->{ //for each market segment
							//establish a trip converter
							DepartureArrivalConverter converter = new DepartureArrivalConverter(depRates.get(tp).get(segmentEntry.getKey()), arrRates.get(tp).get(segmentEntry.getKey()));
							return segmentEntry.getValue().parallelStream().map(modalMtx -> //for each modal matrix
									converter.convert(modalMtx, occupancyRates.get(modalMtx.getMode()))	//convert the matrix
							).collect(Collectors.toSet());	//collect into a set
						})) //which is collected into a map (for each market segment)
				))	//which is collected into a map (for each trip purpose)
		));	//which is collected into a map (for each time period)
	}

	class HBTripGen extends Thread {
		private Map<MarketSegment, FrictionFactorMap> pkFFMaps;
		private Map<MarketSegment, AggregatePAMatrix> aggPKMtxs;

		public HBTripGen(Map<MarketSegment, FrictionFactorMap> pkFFMaps) {
			this.pkFFMaps = pkFFMaps;
		}

		public void run() {
			aggPKMtxs = peakDistribution(graph, pkMaps, pkFFMaps);	//TODO separate threading for distributing pkMaps
		}

		public Map<MarketSegment, AggregatePAMatrix> getAggPKMtxs() { return aggPKMtxs; }


	}
}