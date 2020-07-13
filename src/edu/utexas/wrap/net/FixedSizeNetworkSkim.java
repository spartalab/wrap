package edu.utexas.wrap.net;

public class FixedSizeNetworkSkim implements NetworkSkim {
	
	float[][] skimData;

	public FixedSizeNetworkSkim(float[][] skim) {
		skimData = skim;
	}

	@Override
	public float getCost(TravelSurveyZone producer, TravelSurveyZone attractor) {
		// TODO Auto-generated method stub
		return skimData[producer.getOrder()][attractor.getOrder()];
	}

}
