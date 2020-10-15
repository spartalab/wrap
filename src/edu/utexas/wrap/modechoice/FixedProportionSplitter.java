package edu.utexas.wrap.modechoice;

import java.util.stream.Stream;

import java.util.Map;
import edu.utexas.wrap.demand.containers.FixedMultiplierPassthroughModalPAMatrix;
import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.ModalPAMatrix;

/**The purpose of this class is to return
 * a set of modal PA matrices that contain
 * the proportion of trips that are taken for
 * a particular mode.
 *
 * @author Karthik & Rishabh
 *
 */
public class FixedProportionSplitter implements TripInterchangeSplitter {

	private Map<Mode, Float> map;

	public FixedProportionSplitter(Map<Mode, Float> map) {
		this.map = map;
	}

	/**
	 *	This method returns a set of Modal PA matrices
	 *
	 *  This method	essentially iterates through all
	 *  (i,j) pairs for each mode type and then adds the
	 *  respective proportional value to the a new matrix
	 *  which is added to a set.
	 */
	@Override
	public Stream<ModalPAMatrix> split(AggregatePAMatrix aggregate) {
		return map.entrySet().parallelStream()
				.filter(entry -> entry.getValue() > 0)
				.map(entry -> new FixedMultiplierPassthroughModalPAMatrix(entry.getKey(),entry.getValue(),aggregate)
		);
	}
}
