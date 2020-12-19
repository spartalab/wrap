package edu.utexas.wrap.demand;

import java.util.Collection;
import java.util.Map;

import edu.utexas.wrap.net.TravelSurveyZone;

/**A simple map from a node to a demand level
 * @author William
 *
 */
public interface DemandMap {

	/**
	 * @param dest the Node whose demand level is measured
	 * @return the demand at the given Node
	 */
	public float get(TravelSurveyZone dest);

	/**
	 * @return the collection of nodes for which there is demand
	 */
	public Collection<TravelSurveyZone> getZones();
	
	/**
	 * @param dest the Node to whence there is demand
	 * @param demand the amount of demand present at the Node
	 * @return the previous mapping, if there was one present
	 */
	public Float put(TravelSurveyZone dest, Float demand);

	/**
	 * @return whether this Map has entries
	 */
	public boolean isEmpty();

	/**
	 * @return a copy of the DemandMap with all values as doubles
	 */
	public Map<TravelSurveyZone, Double> doubleClone();

	public default double totalDemand() {
		return getZones().stream().mapToDouble(this::get).sum();
	};
}
