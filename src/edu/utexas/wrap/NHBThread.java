package edu.utexas.wrap;

import java.util.Collection;
import java.util.Map;

import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.ModalPAMatrix;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.marketsegmentation.MarketSegment;
import edu.utexas.wrap.net.Graph;

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
		Map<TripPurpose,PAMap> nhbMaps = NCTCOGTripGen.tripGeneratorNHB(graph,hbMaps);
		nhbBalance(graph, nhbMaps);
		Map<TripPurpose,AggregatePAMatrix> nhbMatrices = nhbTripDist(nhbMaps, nhbFFMaps);
		combineNHBPurposes(nhbMatrices);
		Map<TripPurpose,Collection<ModalPAMatrix>> nhbModalMtxs = nhbModeChoice(nhbMatrices);
		nhbODs = nhbPA2OD(nhbModalMtxs);
		//NHB thread ends here
	}
	
	public Map<TimePeriod,Map<TripPurpose,Collection<ODMatrix>>> getODs(){
		return nhbODs;
	}
}