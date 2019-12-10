package edu.utexas.wrap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.utexas.wrap.balancing.Attr2ProdProportionalBalancer;
import edu.utexas.wrap.balancing.TripBalancer;
import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.ModalPAMatrix;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.demand.containers.FixedSizePAMap;
import edu.utexas.wrap.demand.containers.PAPassthroughMap;
import edu.utexas.wrap.distribution.FrictionFactorMap;
import edu.utexas.wrap.distribution.GravityDistributor;
import edu.utexas.wrap.generation.AreaSpecificTripGenerator;
import edu.utexas.wrap.generation.BasicTripGenerator;
import edu.utexas.wrap.generation.RateProportionTripGenerator;
import edu.utexas.wrap.marketsegmentation.MarketSegment;
import edu.utexas.wrap.modechoice.FixedProportionSplitter;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.modechoice.TripInterchangeSplitter;
import edu.utexas.wrap.net.AreaClass;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.util.AggregatePAMatrixCollector;
import edu.utexas.wrap.util.DemandMapCollector;
import edu.utexas.wrap.util.DepartureArrivalConverter;

class NHBThread extends Thread{
	private Graph graph;
	private Map<TimePeriod,Map<TripPurpose,Collection<ODMatrix>>> nhbODs;
	private Map<TripPurpose,Map<MarketSegment,DemandMap>> hbMaps;
	private ModelInput model;
	
	public NHBThread(Graph graph, ModelInput model, Map<TripPurpose,Map<MarketSegment,DemandMap>> hbMaps) {
		this.graph = graph;
		this.model = model;
		this.hbMaps = hbMaps.entrySet().parallelStream().filter(purpose -> 
			purpose.getKey() != TripPurpose.HOME_PBO &&
			purpose.getKey() != TripPurpose.HOME_SRE
				).collect(Collectors.toMap(Entry::getKey, Entry::getValue)); //TODO preprocess this to combine HBSRE and HBPBO into HBOTH maps
		this.hbMaps.put(TripPurpose.HOME_OTH, hbMaps.entrySet().parallelStream()
				.filter(entry ->
			entry.getKey() == TripPurpose.HOME_PBO ||
			entry.getKey() == TripPurpose.HOME_SRE
				)
				.flatMap(entry -> entry.getValue().entrySet().parallelStream())
		.collect(Collectors.groupingBy(Entry::getKey, Collectors.mapping(Entry::getValue, new DemandMapCollector()))));
	}
	
	public void run() {
		//Perform non-home-based trip generation
		System.out.print((System.currentTimeMillis()-wrapNCTCOG.startMS)+" ms\t");
		System.out.println("Generating secondary trips");
		Map<TripPurpose,PAMap> nhbMaps = generate();
		
		//Flatten NHB PA maps before balancing
		nhbMaps = nhbMaps.entrySet().parallelStream().collect(Collectors.toMap(Entry::getKey, 
				entry -> new FixedSizePAMap(entry.getValue())
				));
		
		//Perform NHB trip balancing
		System.out.print((System.currentTimeMillis()-wrapNCTCOG.startMS)+" ms\t");
		System.out.println("Balancing secondary production-attraction maps");
		balance(nhbMaps);
		
		//Perform NHB trip distribution
		System.out.print((System.currentTimeMillis()-wrapNCTCOG.startMS)+" ms\t");
		System.out.println("Performing secondary trip distribution");
		Map<TripPurpose,AggregatePAMatrix> nhbMatrices = distribute(nhbMaps);
		
		//Combine all trip purposes into a single AggregatePAMatrix
		System.out.print((System.currentTimeMillis()-wrapNCTCOG.startMS)+" ms\t");
		System.out.println("Combining secondary trip purposes");
		Map<TripPurpose,AggregatePAMatrix> combinedMatrices = combinePurposes(nhbMatrices);
		
		//Perform NHB mode choice
		System.out.print((System.currentTimeMillis()-wrapNCTCOG.startMS)+" ms\t");
		System.out.println("Performing secondary trip mode choice");
		Map<TripPurpose,Collection<ModalPAMatrix>> nhbModalMtxs = modeChoice(combinedMatrices);
		
		//Perform NHB PA-to-OD matrix conversion
		System.out.print((System.currentTimeMillis()-wrapNCTCOG.startMS)+" ms\t");
		System.out.println("Converting secondary PA matrices to OD matrices");
		paToOD(nhbModalMtxs);
		//NHB Thread ends here
	}
	
	public Map<TimePeriod,Map<TripPurpose,Collection<ODMatrix>>> getODs(){
		return nhbODs;
	}
	
	public Map<TripPurpose,PAMap> generate(){
		
		Map<TripPurpose,TripPurpose> srcPurposes = new HashMap<TripPurpose,TripPurpose>();
		srcPurposes.put(TripPurpose.WORK_WORK,	TripPurpose.HOME_WORK);
		srcPurposes.put(TripPurpose.WORK_ESH,	TripPurpose.HOME_WORK);
		srcPurposes.put(TripPurpose.WORK_OTH,	TripPurpose.HOME_WORK);
		srcPurposes.put(TripPurpose.SHOP_SHOP,	TripPurpose.HOME_SHOP);
		srcPurposes.put(TripPurpose.SHOP_OTH,	TripPurpose.HOME_SHOP);
		srcPurposes.put(TripPurpose.OTH_OTH,	TripPurpose.HOME_OTH);
		
		Map<TripPurpose, DemandMap> secondaryProds = getProds(srcPurposes);

		Map<TripPurpose, DemandMap> secondaryAttrs = getAttrs(srcPurposes);
		
		return merge(srcPurposes.keySet(), secondaryProds, secondaryAttrs);
	}

	private Map<TripPurpose, PAMap> merge(Collection<TripPurpose> srcPurposes,
			Map<TripPurpose, DemandMap> secondaryProds, Map<TripPurpose, DemandMap> secondaryAttrs) {
		return srcPurposes.parallelStream().collect(Collectors.toMap(Function.identity(), 
				purpose -> new PAPassthroughMap(graph, secondaryProds.get(purpose), secondaryAttrs.get(purpose))));
	}

	private Map<TripPurpose, DemandMap> getProds(Map<TripPurpose, TripPurpose> srcPurposes) {
		Map<TripPurpose,Map<MarketSegment,Double>> secondaryProdRates = srcPurposes.keySet().parallelStream()
				.collect(Collectors.toMap(Function.identity(), purpose -> model.getGeneralProdRates(purpose)));
		
		Map<TripPurpose,Map<MarketSegment,Double>> primaryProdRates = srcPurposes.values().parallelStream().distinct()
				.collect(Collectors.toMap(Function.identity(), purpose -> model.getGeneralProdRates(purpose)));
		
		Map<TripPurpose, DemandMap> secondaryProds = srcPurposes.entrySet().parallelStream().collect(Collectors.toMap(Entry::getKey, entry ->{
			Map<MarketSegment,Double> prPuProRates = primaryProdRates.get(entry.getValue());
			Map<MarketSegment,Double> sePuProRates = secondaryProdRates.get(entry.getKey());
			Map<MarketSegment,DemandMap> puMaps = hbMaps.get(entry.getValue());
			
			RateProportionTripGenerator generator = new RateProportionTripGenerator(graph, prPuProRates, sePuProRates, puMaps);
			return hbMaps.get(entry.getValue()).entrySet().parallelStream()
			.map(segEntry -> generator.generate(segEntry.getValue(), segEntry.getKey()))
			.collect(new DemandMapCollector());
		}));
		return secondaryProds;
	}

	private Map<TripPurpose, DemandMap> getAttrs(Map<TripPurpose, TripPurpose> source) {
		Map<TripPurpose,Map<MarketSegment,Map<AreaClass,Double>>> secondaryAttrRates = source.keySet().parallelStream()
				.collect(Collectors.toMap(Function.identity(), purpose -> model.getAreaClassAttrRates(purpose)));
		
		Map<TripPurpose, DemandMap> secondaryAttrs = source.keySet().parallelStream().collect(Collectors.toMap(Function.identity(), purpose ->{
			Map<MarketSegment,Map<AreaClass,Double>> rates =  secondaryAttrRates.get(purpose);
			BasicTripGenerator generator = new AreaSpecificTripGenerator(graph, rates);
			return rates.keySet().parallelStream().map(seg -> generator.generate(seg)).collect(new DemandMapCollector());
		}));
		return secondaryAttrs;
	}
	
	public void balance(Map<TripPurpose,PAMap> nhbMaps) {
		TripBalancer balancer = new Attr2ProdProportionalBalancer();
		nhbMaps.values().parallelStream().forEach( map -> balancer.balance(map));
	}
	
	public Map<TripPurpose,AggregatePAMatrix> distribute(Map<TripPurpose,PAMap> paMaps) {
		Map<TripPurpose,FrictionFactorMap> ffs = Stream.of(
//				TripPurpose.NONHOME_EDU,
				TripPurpose.OTH_OTH,
				TripPurpose.SHOP_OTH,
				TripPurpose.SHOP_SHOP,
				TripPurpose.WORK_ESH,
				TripPurpose.WORK_OTH,
				TripPurpose.WORK_WORK).parallel()
				.collect(Collectors.toMap(Function.identity(), purpose -> model.getFrictionFactors(purpose, TimePeriod.EARLY_OP, null)));
		
		return paMaps.entrySet().parallelStream().collect(Collectors.toMap(Entry::getKey, entry->
			 new GravityDistributor(graph, ffs.get(entry.getKey())).distribute(entry.getValue())
		));
	}
	
	public Map<TripPurpose,AggregatePAMatrix> combinePurposes(Map<TripPurpose,AggregatePAMatrix> oldMatrices){
		Map<TripPurpose,AggregatePAMatrix> ret = new HashMap<TripPurpose,AggregatePAMatrix>();
		
		ret.put(TripPurpose.NONHOME_WORK, 
				oldMatrices.entrySet().parallelStream()
				.filter(entry -> 
					entry.getKey() == TripPurpose.WORK_WORK ||
					entry.getKey() == TripPurpose.WORK_ESH ||
					entry.getKey() == TripPurpose.WORK_OTH
				)
				.map(entry -> entry.getValue())
				.collect(new AggregatePAMatrixCollector())
				);
		
		ret.put(TripPurpose.NONHOME_NONWORK, 
				oldMatrices.entrySet().parallelStream()
				.filter(entry -> 
					entry.getKey() == TripPurpose.SHOP_SHOP ||
					entry.getKey() == TripPurpose.SHOP_OTH ||
					entry.getKey() == TripPurpose.OTH_OTH ||
					entry.getKey() == TripPurpose.NONHOME_EDU ||
					entry.getKey() == TripPurpose.NONHOME_OTH
				)
				.map(entry -> entry.getValue())
				.collect(new AggregatePAMatrixCollector())
				);

		return ret;
	}
	
	public Map<TripPurpose,Collection<ModalPAMatrix>> modeChoice(
			Map<TripPurpose,AggregatePAMatrix> combinedMatrices){
		Map<TripPurpose,Map<Mode,Double>> modalRates = combinedMatrices.keySet().parallelStream()
				.collect(Collectors.toMap(Function.identity(), purpose -> model.getModeShares(purpose).get(0)));
		
		return combinedMatrices.entrySet().parallelStream().collect(Collectors.toMap(Entry::getKey, entry -> {
			TripInterchangeSplitter mc = new FixedProportionSplitter(modalRates.get(entry.getKey()));
			return mc.split(entry.getValue()).collect(Collectors.toSet());
		}));
	}
	
	public void paToOD(
			Map<TripPurpose,Collection<ModalPAMatrix>> map) {
		Map<Mode,Double> occupancyRates = model.getOccupancyRates();
		Map<TripPurpose,Map<TimePeriod,Double>> depRates = 
				map.keySet().parallelStream()
				.collect(Collectors.toMap(Function.identity(), purpose -> model.getDepartureRates(purpose, null))), 
				arrRates = 
				map.keySet().parallelStream()
				.collect(Collectors.toMap(Function.identity(), purpose -> model.getArrivalRates(purpose,null)));
		
		nhbODs = Stream.of(TimePeriod.values()).parallel().collect(Collectors.toMap(Function.identity(), time ->
			map.entrySet().parallelStream().collect(Collectors.toMap(Entry::getKey, purposeEntry ->{
				DepartureArrivalConverter converter = new DepartureArrivalConverter(
						depRates.get(purposeEntry.getKey()).get(time),
						arrRates.get(purposeEntry.getKey()).get(time));
				return purposeEntry.getValue().parallelStream()
				.map(modalMtx -> converter.convert(modalMtx, occupancyRates.get(modalMtx.getMode())))
				.collect(Collectors.toSet());
			}))
		));
	}
}