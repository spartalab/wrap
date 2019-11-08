package edu.utexas.wrap;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.ModalPAMatrix;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.demand.containers.FixedMultiplierPassthroughPAMap;
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
	private Map<MarketSegment, Double> splitRates;
	private Map<TripPurpose,Map<MarketSegment,PAMap>> hbMaps;
	private Map<MarketSegment, PAMap> pkMaps;
	private Collection<MarketSegment> segments;

	public HBThread(Graph graph, Map<MarketSegment, Double> splitRates, Map<TripPurpose, Map<MarketSegment,PAMap>> hbMaps, Collection<MarketSegment> segments) {
		this.graph = graph;
		this.splitRates = splitRates;
		this.hbMaps = hbMaps;
		this.segments = segments;
	}

	public void run() {
		pkMaps = splitHBW(hbMaps, splitRates);

		Map<MarketSegment, FrictionFactorMap> pkFFMaps = null; //TODO
		HBPkGen hbPkGen = new HBPkGen(graph, pkFFMaps);
		hbPkGen.start();

		Map<TripPurpose, Map<MarketSegment, FrictionFactorMap>> opFFMaps = null; //TODO
		HBOpGen hbOpGen = new HBOpGen(graph, hbMaps, opFFMaps);
		hbOpGen.start();

		try {
			hbPkGen.join();
			hbOpGen.join();
		} catch(InterruptedException e) {
			System.out.println("Thread is interrupted.");
		}

		//After distributing over different friction factor maps, the HBW trips are stuck back together and SRE & PBO matrices are combined
		Map<TripPurpose, Map<MarketSegment, AggregatePAMatrix>> aggCombinedMtxs = combineAggregateMatrices(hbPkGen.getAggPKMtxs(), hbOpGen.getAggOPMtxs()); //TODO combine SRE/PBO and HBWPK/OP matrices

		//TODO divide market segments further by vehicles per worker
		Map<MarketSegment, Map<MarketSegment, Map<TravelSurveyZone, Double>>> workerVehicleRates = null;
		Map<TripPurpose, Map<MarketSegment, AggregatePAMatrix>> dividedCombinedMtxs = subdivideSegments(aggCombinedMtxs,workerVehicleRates);

		//TODO combine HNW trip purposes into single HNW trip purpose for all market segments
		Map<TripPurpose, Map<MarketSegment, AggregatePAMatrix>> combinedMtxs = combineHNWPurposes(dividedCombinedMtxs);

		try {
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
		} catch (IOException e) {
			System.out.println("There is an IO Exception in the HB Thread.");
		}
		//thread ends here
	}
	
	public Map<TimePeriod, Map<TripPurpose, Map<MarketSegment, Collection<ODMatrix>>>> getODs(){
		return hbODs;
	}

	private static Map<MarketSegment,PAMap> splitHBW(Map<TripPurpose,Map<MarketSegment,PAMap>> hbMaps, Map<MarketSegment, Double> splitRates) {
		Map<MarketSegment,PAMap> hbwMaps = hbMaps.get(TripPurpose.HOME_WORK);

		Map<MarketSegment,PAMap> pkMaps = new HashMap<MarketSegment,PAMap>();
		Map<MarketSegment,PAMap> opMaps = new HashMap<MarketSegment,PAMap>();

		hbwMaps.keySet().parallelStream().forEach(seg -> {
			PAMap whole = hbwMaps.get(seg);
			double pkShare = splitRates.get(seg);

			PAMap peak = new FixedMultiplierPassthroughPAMap(whole, pkShare);
			PAMap offpeak = new FixedMultiplierPassthroughPAMap(whole, 1-pkShare);

			opMaps.put(seg, offpeak);
			pkMaps.put(seg, peak);
		});

		hbMaps.put(TripPurpose.HOME_WORK, opMaps);
		return pkMaps;
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
		aggMtxs.entrySet().parallelStream().collect(Collectors.toMap(Map.Entry::getKey, purposeMapEntry ->
				purposeMapEntry.getValue().entrySet().parallelStream()
						.collect(Collectors.toMap(Map.Entry::getKey,
								entry -> {
									TripInterchangeSplitter mc = new FixedProportionSplitter(modeShares.get(entry.getKey()));
									return mc.split(entry.getValue()).collect(Collectors.toSet());
								}))
		));
		return null;
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

	class HBPkGen extends Thread {
		private Graph g;
		private Map<MarketSegment, FrictionFactorMap> pkFFMaps;
		private Map<MarketSegment, AggregatePAMatrix> aggPKMtxs;

		public HBPkGen(Graph graph, Map<MarketSegment, FrictionFactorMap> pkFFMaps) {
			this.g = graph;
			this.pkFFMaps = pkFFMaps;
		}

		public void run() {
			aggPKMtxs = peakDistribution(g, pkMaps, pkFFMaps);	//TODO separate threading for distributing pkMaps
		}

		public Map<MarketSegment, AggregatePAMatrix> getAggPKMtxs() { return aggPKMtxs; }
	}

	class HBOpGen extends Thread {
		private Graph g;
		private Map<TripPurpose,Map<MarketSegment,PAMap>> hbMaps;
		private Map<TripPurpose, Map<MarketSegment, FrictionFactorMap>> opFFMaps;
		private Map<TripPurpose, Map<MarketSegment, AggregatePAMatrix>> aggOPMtxs;

		public HBOpGen(Graph graph, Map<TripPurpose,Map<MarketSegment,PAMap>> hbMaps, Map<TripPurpose, Map<MarketSegment, FrictionFactorMap>> opFFMaps) {
			this.g = graph;
			this.hbMaps = hbMaps;
			this.opFFMaps = opFFMaps;
		}

		public void run() {
			try {
				aggOPMtxs = offPeakDistribution(g, hbMaps, opFFMaps);
			} catch (IOException e) {
				System.out.println("There is an IO Exception in the HB Thread's subthread, HBOpGen.");
			}
		}

		public Map<TripPurpose, Map<MarketSegment, AggregatePAMatrix>> getAggOPMtxs() { return aggOPMtxs; }
	}
}