package edu.utexas.wrap.assignment;

import java.util.Collection;
import java.util.Map;

import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.Node;
import edu.utexas.wrap.net.TravelSurveyZone;

/**A label indicating an object may be associated with a link flow in route 
 * choice. Examples include bushes and paths for bush- and path-based 
 * optimizers, respectively.
 * 
 * @author William
 *
 */
public interface AssignmentContainer {

	/**
	 * @return the Mode used for this assignment
	 */
	public Mode vehicleClass();
	
	/**
	 * @return the value of time for this container
	 */
	public Float valueOfTime();

	/**
	 * @return the set of used links in the container
	 */
	public Collection<Link> usedLinks();

	/**
	 * @return the set of link flows from this container
	 */
	public Map<Link, Double> flows();

	public double incurredCost();

	public TravelSurveyZone root();
	
	public double demand(Node n);
	
	public double totalDemand();
	
}
