package edu.utexas.wrap.demand.containers;

import edu.utexas.wrap.demand.AutoDemandMap;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;
/** A mode-specific OD matrix for autos/trucks only.
 * Instances of this class are expected by a 
 * @author William
 *
 */
public class AutoODMatrix extends ModalHashMatrix {
	private final Float vot;
	
	public AutoODMatrix(Graph g, Float vot, Mode c) {
		super(g, c);
		this.vot = vot;
	}

	/* (non-Javadoc)
	 * @see edu.utexas.wrap.demand.containers.ModalHashMatrix#getVOT()
	 */
	@Override
	public float getVOT() {
		return vot;
	}

	/**
	 * So for every origin, there's an associated DemandMap object,
	 * essentially mapping each destination to the ammount of demand
	 * from the origin to the destination using this (Auto) mode,
	 * so this method creates a duplicate DemandMap linked to this
	 * graph (maintains the VOT and graph)
	 * 
	 * @param origin the Node whose associated demand map should be returned
	 * @return a copy of the stored demand map
	 */
	public AutoDemandMap get(TravelSurveyZone origin) {
		return (AutoDemandMap) map.get(origin);
	}
	
	/* (non-Javadoc)
	 * @see edu.utexas.wrap.demand.containers.ModalHashMatrix#put(edu.utexas.wrap.net.Node, edu.utexas.wrap.net.Node, java.lang.Float)
	 */
	@Override
	public void put(TravelSurveyZone origin, TravelSurveyZone destination, Float demand) {
		map.putIfAbsent(origin, new AutoDemandHashMap(getGraph(), this));
		get(origin).put(destination, demand);
	}

}
