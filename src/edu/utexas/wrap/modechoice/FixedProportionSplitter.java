package edu.utexas.wrap.modechoice;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import edu.utexas.wrap.net.Node;

import edu.utexas.wrap.demand.containers.ModalHashMatrix;
import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.MarketSegment;
import edu.utexas.wrap.demand.ModalPAMatrix;

/**The purpose of this class is to return
 * a set of modal PA matrices that contain
 * the proportion of trips that are taken for
 * a particular mode.
 *
 * @author Karthik & Rishabh
 *
 */
public class FixedProportionSplitter extends TripInterchangeSplitter {

	private Map<MarketSegment, Map<Mode, Float>> map;

	public FixedProportionSplitter(Map<MarketSegment, Map<Mode, Float>> map) {
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
	public Set<ModalPAMatrix> split(AggregatePAMatrix aggregate, MarketSegment ms) {
		Set<ModalPAMatrix> fixedProp = new HashSet<ModalPAMatrix>(map.size());
		Map<Mode, Float> map = this.map.get(ms);
		for (Mode m : map.keySet()) {
			Float pct = map.get(m);
			if (pct <= 0.0) continue;
			
			// Everything between here and the line marked !!!!! can (and should) be parallelized, I think. -Wm
			ModalPAMatrix pa = new ModalHashMatrix(aggregate.getGraph(), m);
			
			for (Node orig : aggregate.getProducers()) {
			// TODO Consider replacing with Strassen's algorithm
				DemandMap origProds = aggregate.getDemandMap(orig);
				for (Node dest : origProds.getNodes()) {
					float demand = aggregate.getDemand(orig, dest);
					pa.put(orig, dest, demand*pct);
				}
			}
			// !!!!!
			
			fixedProp.add(pa);
		}
		return fixedProp;
	}

}
