package edu.utexas.wrap.modechoice;

import java.util.stream.Stream;

import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.ModalPAMatrix;

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
public interface TripInterchangeSplitter {
	
	public Stream<ModalPAMatrix> split(AggregatePAMatrix aggregate);
	
}
