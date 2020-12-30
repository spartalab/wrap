package edu.utexas.wrap.net;

/**A NetworkSkim implementation which stores the full matrix in memory vectors
 * 
 * This implementation provides a re-writable matrix which stores cost data as
 * float vectors.
 * 
 * @author William
 *
 */
public class FixedSizeNetworkSkim implements NetworkSkim {
	
	float[][] skimData;

	/**Construct a fixed skim from pre-existing matrix
	 * 
	 * @param skim
	 */
	public FixedSizeNetworkSkim(float[][] skim) {
		skimData = skim;
	}
	
	/**Create an empty matrix of a given n*n size
	 * @param numZones the number of zones n whose data will be stored in this skim
	 */
	public FixedSizeNetworkSkim(int numZones) {
		skimData = new float[numZones][numZones];
	}

	public float getCost(TravelSurveyZone producer, TravelSurveyZone attractor) {
		return skimData[producer.getOrder()][attractor.getOrder()];
	}
	
	/**Overwrite this skim's cost for a given zone pair
	 * @param orig the origin of trips for this skim cost
	 * @param dest the destination of trips for this skim cost
	 * @param cost the cost (defined arbitrarily) of travel from the origin to the destination
	 */
	public void putCost(TravelSurveyZone orig, TravelSurveyZone dest, float cost) {
		skimData[orig.getOrder()][dest.getOrder()] = cost;
	}

}
