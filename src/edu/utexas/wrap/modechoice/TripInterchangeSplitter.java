package edu.utexas.wrap.modechoice;

import java.util.stream.Stream;

import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.ModalPAMatrix;
import edu.utexas.wrap.marketsegmentation.MarketSegment;

/**The default method of performing mode choice,
 * this method calculates mode shares as a function
 * of the distributed trip matrix (as opposed to just
 * raw, unlinked productions and attractions). This 
 * model assumes that trips are not bound to a given
 * mode, i.e. they are not captive riders.
 * 
 * @author William
 *
 */
public abstract class TripInterchangeSplitter {
	
	public abstract Stream<ModalPAMatrix> split(AggregatePAMatrix aggregate, MarketSegment segment);
	
}
