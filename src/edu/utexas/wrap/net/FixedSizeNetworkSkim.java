package edu.utexas.wrap.net;

public class FixedSizeNetworkSkim implements NetworkSkim {
	
	float[][] skimData;

	public FixedSizeNetworkSkim(float[][] skim) {
		skimData = skim;
	}
	
	public FixedSizeNetworkSkim(int numZones) {
		skimData = new float[numZones][numZones];
	}

	public float getCost(TravelSurveyZone producer, TravelSurveyZone attractor) {
		// TODO Auto-generated method stub
		return skimData[producer.getOrder()][attractor.getOrder()];
	}
	
	public void putCost(TravelSurveyZone orig, TravelSurveyZone dest, float cost) {
		skimData[orig.getOrder()][dest.getOrder()] = cost;
	}

}
