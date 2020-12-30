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

	public FixedSizeNetworkSkim(float[][] skim) {
		skimData = skim;
	}
	
	public FixedSizeNetworkSkim(int numZones) {
		skimData = new float[numZones][numZones];
	}

	public float getCost(TravelSurveyZone producer, TravelSurveyZone attractor) {
		return skimData[producer.getOrder()][attractor.getOrder()];
	}
	
	public void putCost(TravelSurveyZone orig, TravelSurveyZone dest, float cost) {
		skimData[orig.getOrder()][dest.getOrder()] = cost;
	}

}
