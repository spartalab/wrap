package edu.utexas.wrap.demand.containers;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.ModalPAMatrix;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.TravelSurveyZone;

public class ModalHashMatrix implements ODMatrix, ModalPAMatrix {
	
	private final Mode m;
	protected Map<TravelSurveyZone,DemandMap> map;
	
	public ModalHashMatrix(Mode mode) {
		this.m = mode;
		map = new ConcurrentHashMap<TravelSurveyZone, DemandMap>();
	}

 	/* (non-Javadoc)
 	 * @see edu.utexas.wrap.demand.ODMatrix#getMode()
 	 */
 	public Mode getMode() {
 		return m;
 	}

	/* (non-Javadoc)
	 * @see edu.utexas.wrap.demand.ODMatrix#getDemand(edu.utexas.wrap.net.Node, edu.utexas.wrap.net.Node)
	 */
	public float getDemand(TravelSurveyZone origin, TravelSurveyZone destination) {
		return map.get(origin) == null? 0.0F : map.get(origin).get(destination);
	}

	/* (non-Javadoc)
	 * @see edu.utexas.wrap.demand.ODMatrix#put(edu.utexas.wrap.net.Node, edu.utexas.wrap.net.Node, java.lang.Float)
	 */
	@Override
	public void put(TravelSurveyZone prod, TravelSurveyZone attr, Float demand) {
		map.putIfAbsent(prod, new DemandHashMap(getZones()));
		map.get(prod).put(attr, demand);
		
	}

	/**
	 * @param i the Node from which trips originate
	 * @param d the map of demand from the given Node to other Nodes
	 */
	public void putDemand(TravelSurveyZone i, DemandHashMap d) {
		map.put(i, d);
	}

	@Override
	public DemandMap getDemandMap(TravelSurveyZone producer) {
		return map.get(producer);
	}

	@Override
	public Collection<TravelSurveyZone> getProducers() {
		return map.keySet();
	}


	@Override
	public Collection<TravelSurveyZone> getZones() {
		// TODO Auto-generated method stub
		throw new RuntimeException("Not yet implemented");
	}


}
