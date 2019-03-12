package edu.utexas.wrap.assignment;
 
import edu.utexas.wrap.demand.containers.AutoODHashMatrix;
import edu.utexas.wrap.demand.containers.AutoDemandHashMap;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;

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
	
	protected abstract void addAll(AutoODHashMatrix od);

	public abstract void add(Node root, AutoDemandHashMap split);
	
	public abstract void load(Node root);
	
}
