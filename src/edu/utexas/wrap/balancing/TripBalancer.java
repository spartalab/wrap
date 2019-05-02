package edu.utexas.wrap.balancing;

import edu.utexas.wrap.demand.PAMap;

public interface TripBalancer {

    /**
     * Balance Trips such that the total number of productions is equivalent to the total number of attractions
     */
    public void balance(PAMap paMap);
}
