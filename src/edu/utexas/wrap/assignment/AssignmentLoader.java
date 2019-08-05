package edu.utexas.wrap.assignment;
 
import edu.utexas.wrap.demand.containers.AutoODMatrix;
import edu.utexas.wrap.demand.AutoDemandMap;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;
import edu.utexas.wrap.net.TravelSurveyZone;

/** A method of loading an origin-destination matrix or individual
 * origin's demand  onto a roadway network for use in route choice.
 * Demand is added to the loader, then each origin's demand is loaded
 * using the {@code load()} method.
 * 
 * @author William
 *
 */
public abstract class AssignmentLoader {
	protected Graph graph;
	
	protected AssignmentLoader(Graph g) {
		this.graph = g;
	}
	
	/**Load all demand from an OD matrix
	 * @param od the ODMatrix to be loaded into the network
	 */
	protected abstract void submitAll(AutoODMatrix od);

	/** add a given node's DemandHashMap to the pool of demand to be loaded onto the network 
	 * TODO: figure out AutoDemand vs general Demand and generalizing to any Map
	 * @param root the Node from which the demand should emanate
	 * @param split the demand from the given node to any other Node in the network
	 */
	public abstract void submit(TravelSurveyZone root, AutoDemandMap split);
	
	/**
	 * @param root begin the process of loading this Node's demand onto the network 
	 */
	public abstract void start(TravelSurveyZone root);
	
}
