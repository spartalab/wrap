package edu.utexas.wrap.net;

import edu.utexas.wrap.TimePeriod;

public class FixedSizeNetworkSkim implements NetworkSkim {
	
	float[][] skimData;

	public FixedSizeNetworkSkim(float[][] skim) {
		skimData = skim;
	}

	@Override
	public TimePeriod timePeriod() {
		// TODO Auto-generated method stub
		return null;
	}

}
