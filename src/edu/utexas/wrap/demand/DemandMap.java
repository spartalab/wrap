package edu.utexas.wrap.demand;

import java.util.Collection;
import java.util.Map;

import edu.utexas.wrap.net.Graph;
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
	public Double get(TravelSurveyZone dest);

	/**
	 * @return the associated graph
	 */
	public Graph getGraph();

	/**
	 * @return the collection of nodes for which there is demand
	 */
	public Collection<TravelSurveyZone> getZones();

	/** Returns the demand at that Node. If there is no demand
	 * mapping at that node, return the default value given.
	 * 
	 * @param node the Node whose demand is desired
	 * @param f the default value returned if not mapped to a demand
	 * @return the demand level, or the default if no mapping is available
	 */
	public Double getOrDefault(TravelSurveyZone node, Double f);
	
	/**
	 * @param dest the Node to whence there is demand
	 * @param demand the amound of demand present at the Node
	 * @return the previous mapping, if there was one present
	 */
	public Double put(TravelSurveyZone dest, Double demand);

	/**
	 * @return whether this Map has entries
	 */
	public boolean isEmpty();

	/**
	 * @return a copy of the DemandMap with all values as doubles
	 */
	public Map<TravelSurveyZone, Double> doubleClone();
}
