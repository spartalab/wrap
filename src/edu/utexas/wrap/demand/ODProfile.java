package edu.utexas.wrap.demand;

import edu.utexas.wrap.TimePeriod;
import edu.utexas.wrap.modechoice.Mode;

public interface ODProfile {

	public ODMatrix getMatrix(TimePeriod period);
	
	public Mode getMode();

	public Float getVOT(TimePeriod timePeriod);
	
}