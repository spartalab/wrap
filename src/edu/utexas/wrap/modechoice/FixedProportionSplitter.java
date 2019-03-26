package edu.utexas.wrap.modechoice;

import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import edu.utexas.wrap.net.Node;

import edu.utexas.wrap.demand.containers.ModalHashMatrix;
import it.unimi.dsi.fastutil.Hash;
import javafx.util.Pair;
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
public class FixedProportionSplitter extends TripInterchangeSplitter {

	private HashMap<Mode, HashMap<Pair<Node, Node>, Float>> map;

	public FixedProportionSplitter(HashMap<Mode, HashMap<Pair<Node, Node>, Float>> map) {
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
	public Set<ModalPAMatrix> split(AggregatePAMatrix aggregate) {
		// TODO Auto-generated method stub
		Set<ModalPAMatrix> fixedProp = new HashSet<>();
		for (Mode m : map.keySet()) {
			ModalPAMatrix pa = new ModalHashMatrix(aggregate.getGraph(), aggregate.getMode());
			for (Pair p : map.get(m).keySet()) {
				if (map.get(m).get(p) != 0.0) {
					float proportion = map.get(m).get(p);
					float demand = aggregate.getDemand(((Node)p.getKey()), ((Node)p.getValue()));
					pa.put(((Node)p.getKey()), ((Node)p.getValue()), demand*proportion);
				}
			}
			fixedProp.add(pa);
		}
		return fixedProp;
	}

}
