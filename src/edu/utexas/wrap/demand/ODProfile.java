package edu.utexas.wrap.demand;

import edu.utexas.wrap.TimePeriod;

public interface ODProfile {

	public ODMatrix getMatrix(TimePeriod period);
}
