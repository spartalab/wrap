package edu.utexas.wrap.net;

import edu.utexas.wrap.assignment.AssignmentContainer;
import edu.utexas.wrap.modechoice.Mode;

/**A link through which trip demand begins and enters from a TravelSurveyZone
 * 
 * For all trips across a network, there must be a defined entry and exit point
 * to/from the network. These are TravelSurveyZones, the origin and destination
 * of every trip. To link these TravelSurveyZones to the network, dummy Nodes are
 * created, called centroids, which are not true junction points in roadway nets,
 * but rather can be thought of as the collection of driveways and parking lots
 * in a zone.
 * 
 * These links are generally not congestible, with an exceptionally high capacity
 * usually defined, and a fixed travel time being returned regardless of the amount
 * of flow on the Centroid. To ensure these connectors are not used as through-ways
 * in trips, care must be taken that all trips assigned to this link either start
 * or end at one of the termini of the link.
 * 
 * @author William
 *
 */
public class CentroidConnector extends TolledLink {
	private float toll;

	public CentroidConnector(Node tail, Node head, Float capacity, Float length, Float fftime, Float toll, Integer linkID) {
		super(tail, head, capacity, length, fftime, linkID);
		this.toll = toll;
	}

	public Boolean allowsClass(Mode c) {
		return true;
	}

	public double getPrice(Float vot, Mode c) {
		return (double) (vot*freeFlowTime() + toll);
	}

	public Float getToll(Mode c) {
		if (!allowsClass(c)) return Float.MAX_VALUE;
		return toll;
	}

	public double getTravelTime() {
		return (double) freeFlowTime();
	}

	public double pricePrime(Float vot) {
		return 0.0;
	}

	public double tIntegral() {
		return freeFlowTime()*getFlow();
	}

	public double tollPrime() {
		return 0.0;
	}

	public double tPrime() {
		return 0.0;
	}

	public double getPrice(AssignmentContainer container) {
		return getPrice(container.valueOfTime(),container.vehicleClass());
	}

}
