package edu.utexas.wrap.demand.containers;

import java.util.Collection;
import java.util.Map;

import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.ModalPAMatrix;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class ModalHashMatrix  implements ODMatrix, ModalPAMatrix {
	
	private final Mode m;
	private Graph g;
	protected Map<TravelSurveyZone,DemandMap> map;
	private float vot;
	
	public ModalHashMatrix(Graph g, Mode mode) {
		this.g = g;
		this.m = mode;
		map = Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap<TravelSurveyZone, DemandMap>());
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
	public Float getDemand(TravelSurveyZone origin, TravelSurveyZone destination) {
		return map.get(origin) == null? 0.0F : map.get(origin).getOrDefault(destination,0.0).floatValue();
	}

	/* (non-Javadoc)
	 * @see edu.utexas.wrap.demand.ODMatrix#put(edu.utexas.wrap.net.Node, edu.utexas.wrap.net.Node, java.lang.Float)
	 */
	@Override
	public void put(TravelSurveyZone prod, TravelSurveyZone attr, Float demand) {
		map.putIfAbsent(prod, new DemandHashMap(getGraph()));
		map.get(prod).put(attr, demand.doubleValue());
		
	}

	/**
	 * @param i the Node from which trips originate
	 * @param d the map of demand from the given Node to other Nodes
	 */
	public void putDemand(TravelSurveyZone i, DemandHashMap d) {
		map.put(i, d);
	}

	/* (non-Javadoc)
	 * @see edu.utexas.wrap.demand.PAMatrix#getVOT()
	 */
	@Override
	public Float getVOT() {
		return vot;
	}

	/* (non-Javadoc)
	 * @see edu.utexas.wrap.demand.ODMatrix#getGraph()
	 */
	@Override
	public Graph getGraph() {
		return g;
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
	public void setVOT(float VOT) {
		vot = VOT;
	}


}
