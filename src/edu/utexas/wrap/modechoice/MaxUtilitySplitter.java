package edu.utexas.wrap.modechoice;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.ModalPAMatrix;
import edu.utexas.wrap.demand.containers.AggregatePAHashMatrix;
import edu.utexas.wrap.demand.containers.ModalHashMatrix;
import edu.utexas.wrap.net.Node;

public class MaxUtilitySplitter extends TripInterchangeSplitter {
	private UtilityModel utility;
	
	public MaxUtilitySplitter(UtilityModel model) {
		utility = model;
	} 
	
	@Override
	public Set<ModalPAMatrix> split(AggregatePAMatrix aggregate) {
		// TODO Auto-generated method stub
		Map<Mode, ModalPAMatrix> map = new HashMap<Mode,ModalPAMatrix>();
		for (Node origin : (aggregate.keySet())) {
			for (Node dest : (aggregate.get(origin).keySet())) {
				Double util = null;
				Mode mode = null;
				
				for (Mode m : utility.getModes()) {
					if (util == null || utility.getUtility(m, origin, dest) > util) {
						util = utility.getUtility(m, origin, dest);
						mode = m;
					}
				}
				
				map.putIfAbsent(mode, new ModalHashMatrix(aggregate.getGraph(),mode));
			}
			
		}
		
		return map;
	}

}
