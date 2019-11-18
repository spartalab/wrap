package edu.utexas.wrap;

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
import edu.utexas.wrap.util.AggregatePAMatrixCollector;
import edu.utexas.wrap.util.DepartureArrivalConverter;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class HBThread extends Thread{
	private Graph graph;
	private Map<TimePeriod, Map<TripPurpose, Map<MarketSegment, Collection<ODMatrix>>>> hbODs;
	private Map<TripPurpose,Map<MarketSegment,PAMap>> hbMaps;
	private ModelInput model;

	public HBThread(Graph graph, ModelInput model, Map<TripPurpose, Map<MarketSegment,PAMap>> hbMaps) {
		this.graph = graph;
		this.hbMaps = hbMaps;
		this.model = model;
	}

	public void run() {
		Map<MarketSegment, PAMap> pkMaps = extractPeakTrips(hbMaps);

		HBPkGen hbPkGen = new HBPkGen(graph, pkMaps);
		hbPkGen.start();

		HBOpGen hbOpGen = new HBOpGen(graph, hbMaps);
		hbOpGen.start();

		try {
			hbPkGen.join();
			hbOpGen.join();
		} catch(InterruptedException e) {
			System.err.println("Thread is interrupted.");
			e.printStackTrace();
		}

		//After distributing over different friction factor maps, the HBW trips are stuck back together and SRE & PBO matrices are combined
		Map<TripPurpose, Map<MarketSegment, AggregatePAMatrix>> aggCombinedMtxs = combineAggregateMatrices(hbPkGen.getAggPKMtxs(), hbOpGen.getAggOPMtxs()); //TODO combine SRE/PBO and HBWPK/OP matrices

		//divide market segments further by vehicles per worker
		Map<TripPurpose, Map<MarketSegment, AggregatePAMatrix>> dividedCombinedMtxs = subdivideSegments(aggCombinedMtxs);

		//combine HNW trip purposes into single HNW trip purpose for all market segments
		Map<TripPurpose, Map<MarketSegment, AggregatePAMatrix>> combinedMtxs = combineHNWPurposes(dividedCombinedMtxs);

		try {
			//Perform mode choice splitting TODO ensure proper use of market segments
			Map<TripPurpose, Map<MarketSegment, Collection<ModalPAMatrix>>> hbModalMtxs = modeChoice(combinedMtxs);

			//PA to OD splitting by time of day
			hbODs = paToODConversion(hbModalMtxs);
		} catch (IOException e) {
			System.err.println("There is an IO Exception in the HB Thread.");
			e.printStackTrace();
		}
		//thread ends here
	}
	
	public Map<TimePeriod, Map<TripPurpose, Map<MarketSegment, Collection<ODMatrix>>>> getODs(){
		return hbODs;
	}

	private Map<MarketSegment,PAMap> extractPeakTrips(Map<TripPurpose,Map<MarketSegment,PAMap>> hbMaps) {
		Map<MarketSegment,PAMap> hbwMaps = hbMaps.get(TripPurpose.HOME_WORK);
		Map<MarketSegment,Double> splitRates = model.getPeakShares(TripPurpose.HOME_WORK);

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

	private Map<MarketSegment, AggregatePAMatrix> peakDistribution(
			Graph graph,
			Map<MarketSegment, PAMap> pkMaps,
			Map<MarketSegment, FrictionFactorMap> pkFFMaps) {
		return pkMaps.entrySet().parallelStream().collect(Collectors.toMap(Map.Entry::getKey, entry->{
			TripDistributor distributor = new GravityDistributor(graph, pkFFMaps.get(entry.getKey()));
			return distributor.distribute(entry.getValue());
		}));
	}

	private Map<TripPurpose, Map<MarketSegment, AggregatePAMatrix>> offPeakDistribution(
			Graph g,
			Map<TripPurpose, Map<MarketSegment, PAMap>> hbMaps
			// TODO: consider if this (and pk distribution) should take in a mapping directly to the distributor
			// (maybe some purpose-segment pairs have the same friction factor map? So this would be unnecessary)
	) {

		return hbMaps.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, purposeEntry ->
				purposeEntry.getValue().entrySet().parallelStream()
						.collect(Collectors.toMap(Map.Entry::getKey, segmentEntry -> {
							TripDistributor distributor = new GravityDistributor(g, model.getFrictionFactors(purposeEntry.getKey(), TimePeriod.EARLY_OP, segmentEntry.getKey()));
							return distributor.distribute(segmentEntry.getValue());
						}))
		));
	}

	private Map<TripPurpose, Map<MarketSegment, AggregatePAMatrix>> combineAggregateMatrices(
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

	private Map<TripPurpose, Map<MarketSegment, AggregatePAMatrix>> subdivideSegments(Map<TripPurpose, Map<MarketSegment, AggregatePAMatrix>> aggCombinedMtxs) {
		
		return aggCombinedMtxs.entrySet().parallelStream().collect(Collectors.toMap(Map.Entry::getKey, purposeMapEntry-> //for each trip purpose
				purposeMapEntry.getValue().entrySet().parallelStream() //Stream of MarketSegment-Matrix entries
						.map(oldSegmentMap ->	//Take each segment-matrix pair
								model.getWorkerVehicleSplits(oldSegmentMap.getKey(),purposeMapEntry.getKey()).entrySet().parallelStream()		//get a stream of all related segment-rateMap pairs
										.collect(Collectors.toMap(Map.Entry::getKey, 										//map each related (more bespoke) segment to
												rateMap ->  new PerProductionZoneMultiplierPassthroughMatrix(oldSegmentMap.getValue(),rateMap.getValue())))	//a rate multiplier map
						).flatMap(map -> map.entrySet().parallelStream()) 			//unpackage the map to a set of entries
						.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))	//collect all the entries together in a single map
		//This assumes no duplicates exist in the more bespoke (resulting) market segments

		));


	}

	private Map<TripPurpose, Map<MarketSegment, AggregatePAMatrix>> combineHNWPurposes(Map<TripPurpose, Map<MarketSegment, AggregatePAMatrix>> aggCombinedMtxs) {
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

	private Map<TripPurpose,Map<MarketSegment, Collection<ModalPAMatrix>>> modeChoice(Map<TripPurpose,Map<MarketSegment, AggregatePAMatrix>> aggMtxs) throws IOException {
		Map<TripPurpose,Map<MarketSegment, Map<Mode, Double>>> modeShares = aggMtxs.keySet().parallelStream()
				.collect(Collectors.toMap(Function.identity(), purpose -> model.getModeShares(purpose)));
		
		return aggMtxs.entrySet().parallelStream().collect(Collectors.toMap(Map.Entry::getKey, purposeMapEntry ->
				purposeMapEntry.getValue().entrySet().parallelStream()
						.collect(Collectors.toMap(Map.Entry::getKey,
								entry -> {
									TripInterchangeSplitter mc = new FixedProportionSplitter(modeShares.get(purposeMapEntry.getKey()).get(entry.getKey()));
									return mc.split(entry.getValue()).collect(Collectors.toSet());
								}))
		));
	}

	private Map<TimePeriod,Map<TripPurpose, Map<MarketSegment,Collection<ODMatrix>>>> paToODConversion(Map<TripPurpose, Map<MarketSegment, Collection<ModalPAMatrix>>> hbModalMtxs) throws IOException {
		Map<Mode, Double> occupancyRates = model.getOccupancyRates(); // modalOccRates.csv
		//TOD splitting inputs TODO ensure proper use of market segments
		Map<TripPurpose,Map<MarketSegment, Map<TimePeriod, Double>>>
				depRates = 
				hbModalMtxs.entrySet().parallelStream()
				.collect(Collectors.toMap(Entry::getKey, entry ->
					entry.getValue().keySet().parallelStream().collect(Collectors.toMap(Function.identity(), seg -> model.getDepartureRates(entry.getKey(),seg))))),
				arrRates = hbModalMtxs.entrySet().parallelStream()
					.collect(Collectors.toMap(Entry::getKey, entry ->
					entry.getValue().keySet().parallelStream().collect(Collectors.toMap(Function.identity(), seg -> model.getArrivalRates(entry.getKey(),seg)))));
		
		
		return Stream.of(TimePeriod.values()).collect(Collectors.toMap(Function.identity(), time -> //for each time period
				hbModalMtxs.entrySet().parallelStream().collect(Collectors.toMap(Map.Entry::getKey, purposeEntry -> //for each trip purpose
						purposeEntry.getValue().entrySet().parallelStream().collect(Collectors.toMap(Map.Entry::getKey, segmentEntry ->{ //for each market segment
							//establish a trip converter
							DepartureArrivalConverter converter = new DepartureArrivalConverter(
									depRates.get(purposeEntry.getKey()).get(segmentEntry.getKey()).get(time), 
									arrRates.get(purposeEntry.getKey()).get(segmentEntry.getKey()).get(time)
									);
							return segmentEntry.getValue().parallelStream().map(modalMtx -> //for each modal matrix
									converter.convert(modalMtx, occupancyRates.get(modalMtx.getMode()))	//convert the matrix
							).collect(Collectors.toSet());	//collect into a set
						})) //which is collected into a map (for each market segment)
				))	//which is collected into a map (for each trip purpose)
		));	//which is collected into a map (for each time period)
	}

	class HBPkGen extends Thread {
		private Graph g;
		private Map<MarketSegment, FrictionFactorMap> frictionFactorMaps;
		private Map<MarketSegment, AggregatePAMatrix> aggregateMatrices;
		private Map<MarketSegment, PAMap> pkMaps;

		public HBPkGen(Graph graph,
				Map<MarketSegment, PAMap> pkMaps) {
			this.g = graph;
			this.pkMaps = pkMaps;
;
		}

		public void run() {
			frictionFactorMaps = pkMaps.keySet().stream().collect(Collectors.toMap(Function.identity(), seg -> model.getFrictionFactors(TripPurpose.HOME_WORK, TimePeriod.AM_PK, seg)));
			aggregateMatrices = peakDistribution(g, pkMaps, frictionFactorMaps);
		}

		public Map<MarketSegment, AggregatePAMatrix> getAggPKMtxs() { return aggregateMatrices; }
	}

	class HBOpGen extends Thread {
		private Graph g;
		private Map<TripPurpose,Map<MarketSegment,PAMap>> hbMaps;
		private Map<TripPurpose, Map<MarketSegment, AggregatePAMatrix>> aggOPMtxs;

		public HBOpGen(Graph graph, Map<TripPurpose,Map<MarketSegment,PAMap>> hbMaps) {
			this.g = graph;
			this.hbMaps = hbMaps;
		}

		public void run() {
			aggOPMtxs = offPeakDistribution(g, hbMaps);
		}

		public Map<TripPurpose, Map<MarketSegment, AggregatePAMatrix>> getAggOPMtxs() { return aggOPMtxs; }
	}
}