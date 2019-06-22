package edu.utexas.wrap.assignment;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;

import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.Node;

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
	public Mode getVehicleClass();
	
	/**
	 * @return the value of time for this container
	 */
	public Float getVOT();

	/**
	 * @param n the Node whose demand should be returned
	 * @return the demand level at that node
	 */
	public Float getDemand(Node n);
	
	/**
	 * @return the set of used links in the container
	 */
	public Set<Link> getLinks();

	/**
	 * @param l the link whose flow should be measured
	 * @return the flow on a given link from this container
	 */
	@Deprecated
	public Double getFlow(Link l);

	/**
	 * @return the set of link flows from this container
	 */
	public Map<Link, Double> getFlows();
	
	/**Read an assignment container from an input stream
	 * @param in the stream providing a byte-level representation of the links used by the container
	 * @throws IOException
	 */
	void fromFile(BufferedInputStream in) throws IOException;
	
	/**Write a byte-level representation of the container's links to an ouptut stream
	 * @param out the stream to which the assignment container will be output
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void toFile(OutputStream out) throws IOException, InterruptedException;
	
}
