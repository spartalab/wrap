package edu.utexas.wrap.demand.containers;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.PAMatrix;
import edu.utexas.wrap.net.TravelSurveyZone;

public class AggregatePAHashMatrix implements AggregatePAMatrix {

//	private Graph g;
	private final Collection<TravelSurveyZone> zones;
	private Map<TravelSurveyZone,DemandMap> matrix;

	public AggregatePAHashMatrix(Collection<TravelSurveyZone> zones) {
		this.zones = zones;
		matrix = new ConcurrentHashMap<TravelSurveyZone,DemandMap>(zones.size(),1.0f);
	}

	public AggregatePAHashMatrix(PAMatrix hbwSum, Map<TravelSurveyZone, Float> map) {
		zones = hbwSum.getZones();
		matrix = new ConcurrentHashMap<TravelSurveyZone,DemandMap>(zones.size(),1.0f);
		hbwSum.getProducers().parallelStream().forEach(prod ->{
			matrix.put(prod, new FixedMultiplierPassthroughDemandMap(hbwSum.getDemandMap(prod),map.getOrDefault(prod,0.0f)));
		});
	}

	/**
	 * Insert the demand map for a given node
	 * @param i the Node from which there is demand
	 * @param d the map of demand from the given Node to other Nodes
	 */
	public void putDemandMap(TravelSurveyZone i, DemandMap d) {
		matrix.put(i, d);
	}

	/* (non-Javadoc)
	 * @see edu.utexas.wrap.demand.PAMatrix#getDemand(edu.utexas.wrap.net.Node, edu.utexas.wrap.net.Node)
	 */
	@Override
	public float getDemand(TravelSurveyZone producer, TravelSurveyZone attractor) {
		return matrix.get(producer) == null ? 0.0F : matrix.get(producer).get(attractor);
	}

	/* (non-Javadoc)
	 * @see edu.utexas.wrap.demand.PAMatrix#getGraph()
	 */
	@Override
	public Collection<TravelSurveyZone> getZones(){
		return zones;
	}

	/* (non-Javadoc)
	 * @see edu.utexas.wrap.demand.PAMatrix#put(edu.utexas.wrap.net.Node, edu.utexas.wrap.net.Node, java.lang.Float)
	 */
	@Override
	public void put(TravelSurveyZone producer, TravelSurveyZone attractor, Float demand) {
		matrix.putIfAbsent(producer,new DemandHashMap(zones));
		((DemandMap) matrix.get(producer)).put(attractor,demand);
		
	}

	@Override
	public DemandMap getDemandMap(TravelSurveyZone producer) {
		return matrix.get(producer);
	}

	@Override
	public Collection<TravelSurveyZone> getProducers() {
		return matrix.keySet();
	}

}
