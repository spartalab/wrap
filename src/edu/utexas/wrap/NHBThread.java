package edu.utexas.wrap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import edu.utexas.wrap.balancing.Attr2ProdProportionalBalancer;
import edu.utexas.wrap.balancing.TripBalancer;
import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.ModalPAMatrix;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.distribution.FrictionFactorMap;
import edu.utexas.wrap.distribution.GravityDistributor;
import edu.utexas.wrap.distribution.TripDistributor;
import edu.utexas.wrap.marketsegmentation.MarketSegment;
import edu.utexas.wrap.modechoice.FixedProportionSplitter;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.modechoice.TripInterchangeSplitter;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.util.AggregatePAMatrixCollector;

class NHBThread extends Thread{
	private Graph graph;
	private Map<TimePeriod,Map<TripPurpose,Collection<ODMatrix>>> nhbODs;
	private Map<TripPurpose,Map<MarketSegment,PAMap>> hbMaps;
	
	public NHBThread(Graph graph, Map<TripPurpose,Map<MarketSegment,PAMap>> hbMaps) {
		this.graph = graph;
		this.hbMaps = hbMaps;
	}
	
	public void run() {
		//NHB thread starts here
		//TODO New thread should start here for non-home-based trips
		Map<TripPurpose,PAMap> nhbMaps = generate(graph,hbMaps);
		
		balance(nhbMaps);
		
		Map<TripPurpose,FrictionFactorMap> nhbFFMaps = null;
		Map<TripPurpose,AggregatePAMatrix> nhbMatrices = distribute(nhbMaps, nhbFFMaps);
		
		Map<TripPurpose,AggregatePAMatrix> combinedMatrices = combinePurposes(nhbMatrices);
		
		Map<TripPurpose,Map<Mode,Double>> modalRates = null;
		Map<TripPurpose,Collection<ModalPAMatrix>> nhbModalMtxs = modeChoice(combinedMatrices, modalRates);
		
		nhbODs = pa2od(nhbModalMtxs);
		//NHB thread ends here
	}
	
	public Map<TimePeriod,Map<TripPurpose,Collection<ODMatrix>>> getODs(){
		return nhbODs;
	}
	
	public void balance(Map<TripPurpose,PAMap> nhbMaps) {
		TripBalancer balancer = new Attr2ProdProportionalBalancer();
		nhbMaps.values().parallelStream().forEach( map -> balancer.balance(map));
	}
	
	public Map<TripPurpose,AggregatePAMatrix> distribute(
			Map<TripPurpose,PAMap> paMaps, 
			Map<TripPurpose,FrictionFactorMap> ffs){
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
			Map<TripPurpose,AggregatePAMatrix> combinedMatrices, 
			Map<TripPurpose,Map<Mode,Double>> modalRates){
		return combinedMatrices.entrySet().parallelStream().collect(Collectors.toMap(Entry::getKey, entry -> {
			TripInterchangeSplitter mc = new FixedProportionSplitter(modalRates.get(entry.getKey()));
			return mc.split(entry.getValue()).collect(Collectors.toSet());
		}));
	}
}