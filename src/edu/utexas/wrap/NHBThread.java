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
	private Map<TimePeriod,Map<TripPurposeEnum,Collection<ODMatrix>>> nhbODs;
	private Map<TripPurposeEnum,Map<MarketSegment,DemandMap>> hbMaps;
	private ModelInput model;
	
	public NHBThread(Graph graph, ModelInput model, Map<TripPurposeEnum,Map<MarketSegment,DemandMap>> hbMaps) {
		this.graph = graph;
		this.model = model;
		this.hbMaps = hbMaps.entrySet().parallelStream().filter(purpose -> 
			purpose.getKey() != TripPurposeEnum.HOME_PBO &&
			purpose.getKey() != TripPurposeEnum.HOME_SRE
				).collect(Collectors.toMap(Entry::getKey, Entry::getValue)); //TODO preprocess this to combine HBSRE and HBPBO into HBOTH maps
		this.hbMaps.put(TripPurposeEnum.HOME_OTH, hbMaps.entrySet().parallelStream()
				.filter(entry ->
			entry.getKey() == TripPurposeEnum.HOME_PBO ||
			entry.getKey() == TripPurposeEnum.HOME_SRE
				)
				.flatMap(entry -> entry.getValue().entrySet().parallelStream())
		.collect(Collectors.groupingBy(Entry::getKey, Collectors.mapping(Entry::getValue, new DemandMapCollector()))));
	}
	
	public void run() {
		//Perform non-home-based trip generation
		System.out.print((System.currentTimeMillis()-wrapNCTCOG.startMS)+" ms\t");
		System.out.println("Generating secondary trips");
		Map<TripPurposeEnum,PAMap> nhbMaps = generate();
		
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
		Map<TripPurposeEnum,AggregatePAMatrix> nhbMatrices = distribute(nhbMaps);
		
		//Combine all trip purposes into a single AggregatePAMatrix
		System.out.print((System.currentTimeMillis()-wrapNCTCOG.startMS)+" ms\t");
		System.out.println("Combining secondary trip purposes");
		Map<TripPurposeEnum,AggregatePAMatrix> combinedMatrices = combinePurposes(nhbMatrices);
		
		//Perform NHB mode choice
		System.out.print((System.currentTimeMillis()-wrapNCTCOG.startMS)+" ms\t");
		System.out.println("Performing secondary trip mode choice");
		Map<TripPurposeEnum,Collection<ModalPAMatrix>> nhbModalMtxs = modeChoice(combinedMatrices);
		
		//Perform NHB PA-to-OD matrix conversion
		System.out.print((System.currentTimeMillis()-wrapNCTCOG.startMS)+" ms\t");
		System.out.println("Converting secondary PA matrices to OD matrices");
		paToOD(nhbModalMtxs);
		//NHB Thread ends here
	}
	
	public Map<TimePeriod,Map<TripPurposeEnum,Collection<ODMatrix>>> getODs(){
		return nhbODs;
	}
	
	public Map<TripPurposeEnum,PAMap> generate(){
		
		Map<TripPurposeEnum,TripPurposeEnum> srcPurposes = new HashMap<TripPurposeEnum,TripPurposeEnum>();
		srcPurposes.put(TripPurposeEnum.WORK_WORK,	TripPurposeEnum.HOME_WORK);
		srcPurposes.put(TripPurposeEnum.WORK_ESH,	TripPurposeEnum.HOME_WORK);
		srcPurposes.put(TripPurposeEnum.WORK_OTH,	TripPurposeEnum.HOME_WORK);
		srcPurposes.put(TripPurposeEnum.SHOP_SHOP,	TripPurposeEnum.HOME_SHOP);
		srcPurposes.put(TripPurposeEnum.SHOP_OTH,	TripPurposeEnum.HOME_SHOP);
		srcPurposes.put(TripPurposeEnum.OTH_OTH,	TripPurposeEnum.HOME_OTH);
		
		Map<TripPurposeEnum, DemandMap> secondaryProds = getProds(srcPurposes);

		Map<TripPurposeEnum, DemandMap> secondaryAttrs = getAttrs(srcPurposes);
		
		return merge(srcPurposes.keySet(), secondaryProds, secondaryAttrs);
	}

	private Map<TripPurposeEnum, PAMap> merge(Collection<TripPurposeEnum> srcPurposes,
			Map<TripPurposeEnum, DemandMap> secondaryProds, Map<TripPurposeEnum, DemandMap> secondaryAttrs) {
		return srcPurposes.parallelStream().collect(Collectors.toMap(Function.identity(), 
				purpose -> new PAPassthroughMap(graph, secondaryProds.get(purpose), secondaryAttrs.get(purpose))));
	}

	private Map<TripPurposeEnum, DemandMap> getProds(Map<TripPurposeEnum, TripPurposeEnum> srcPurposes) {
		Map<TripPurposeEnum,Map<MarketSegment,Double>> secondaryProdRates = srcPurposes.keySet().parallelStream()
				.collect(Collectors.toMap(Function.identity(), purpose -> model.getGeneralProdRates(purpose)));
		
		Map<TripPurposeEnum,Map<MarketSegment,Double>> primaryProdRates = srcPurposes.values().parallelStream().distinct()
				.collect(Collectors.toMap(Function.identity(), purpose -> model.getGeneralProdRates(purpose)));
		
		Map<TripPurposeEnum, DemandMap> secondaryProds = srcPurposes.entrySet().parallelStream().collect(Collectors.toMap(Entry::getKey, entry ->{
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

	private Map<TripPurposeEnum, DemandMap> getAttrs(Map<TripPurposeEnum, TripPurposeEnum> source) {
		Map<TripPurposeEnum,Map<MarketSegment,Map<AreaClass,Double>>> secondaryAttrRates = source.keySet().parallelStream()
				.collect(Collectors.toMap(Function.identity(), purpose -> model.getAreaClassAttrRates(purpose)));
		
		Map<TripPurposeEnum, DemandMap> secondaryAttrs = source.keySet().parallelStream().collect(Collectors.toMap(Function.identity(), purpose ->{
			Map<MarketSegment,Map<AreaClass,Double>> rates =  secondaryAttrRates.get(purpose);
			BasicTripGenerator generator = new AreaSpecificTripGenerator(graph, rates);
			return rates.keySet().parallelStream().map(seg -> generator.generate(seg)).collect(new DemandMapCollector());
		}));
		return secondaryAttrs;
	}
	
	public void balance(Map<TripPurposeEnum,PAMap> nhbMaps) {
		TripBalancer balancer = new Attr2ProdProportionalBalancer();
		nhbMaps.values().parallelStream().forEach( map -> balancer.balance(map));
	}
	
	public Map<TripPurposeEnum,AggregatePAMatrix> distribute(Map<TripPurposeEnum,PAMap> paMaps) {
		Map<TripPurposeEnum,FrictionFactorMap> ffs = Stream.of(
//				TripPurpose.NONHOME_EDU,
				TripPurposeEnum.OTH_OTH,
				TripPurposeEnum.SHOP_OTH,
				TripPurposeEnum.SHOP_SHOP,
				TripPurposeEnum.WORK_ESH,
				TripPurposeEnum.WORK_OTH,
				TripPurposeEnum.WORK_WORK).parallel()
				.collect(Collectors.toMap(Function.identity(), purpose -> model.getFrictionFactors(purpose, TimePeriod.EARLY_OP, null)));
		
		return paMaps.entrySet().parallelStream().collect(Collectors.toMap(Entry::getKey, entry->
			 new GravityDistributor(graph, ffs.get(entry.getKey())).distribute(entry.getValue())
		));
	}
	
	public Map<TripPurposeEnum,AggregatePAMatrix> combinePurposes(Map<TripPurposeEnum,AggregatePAMatrix> oldMatrices){
		Map<TripPurposeEnum,AggregatePAMatrix> ret = new HashMap<TripPurposeEnum,AggregatePAMatrix>();
		
		ret.put(TripPurposeEnum.NONHOME_WORK, 
				oldMatrices.entrySet().parallelStream()
				.filter(entry -> 
					entry.getKey() == TripPurposeEnum.WORK_WORK ||
					entry.getKey() == TripPurposeEnum.WORK_ESH ||
					entry.getKey() == TripPurposeEnum.WORK_OTH
				)
				.map(entry -> entry.getValue())
				.collect(new AggregatePAMatrixCollector())
				);
		
		ret.put(TripPurposeEnum.NONHOME_NONWORK, 
				oldMatrices.entrySet().parallelStream()
				.filter(entry -> 
					entry.getKey() == TripPurposeEnum.SHOP_SHOP ||
					entry.getKey() == TripPurposeEnum.SHOP_OTH ||
					entry.getKey() == TripPurposeEnum.OTH_OTH ||
					entry.getKey() == TripPurposeEnum.NONHOME_EDU ||
					entry.getKey() == TripPurposeEnum.NONHOME_OTH
				)
				.map(entry -> entry.getValue())
				.collect(new AggregatePAMatrixCollector())
				);

		return ret;
	}
	
	public Map<TripPurposeEnum,Collection<ModalPAMatrix>> modeChoice(
			Map<TripPurposeEnum,AggregatePAMatrix> combinedMatrices){
		Map<TripPurposeEnum,Map<Mode,Double>> modalRates = combinedMatrices.keySet().parallelStream()
				.collect(Collectors.toMap(Function.identity(), purpose -> model.getModeShares(purpose).get(0)));
		
		return combinedMatrices.entrySet().parallelStream().collect(Collectors.toMap(Entry::getKey, entry -> {
			TripInterchangeSplitter mc = new FixedProportionSplitter(modalRates.get(entry.getKey()));
			return mc.split(entry.getValue()).collect(Collectors.toSet());
		}));
	}
	
	public void paToOD(
			Map<TripPurposeEnum,Collection<ModalPAMatrix>> map) {
		Map<Mode,Double> occupancyRates = model.getOccupancyRates();
		Map<TripPurposeEnum,Map<TimePeriod,Double>> depRates = 
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