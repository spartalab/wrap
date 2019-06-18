package edu.utexas.wrap.demand.containers;

import edu.utexas.wrap.demand.AutoDemandMap;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Graph;

public class AutoFixedSizeDemandMap extends FixedSizeDemandMap implements AutoDemandMap {
	private AutoODMatrix parent;
	
	public AutoFixedSizeDemandMap(Graph g, AutoODMatrix p) {
		super(g);
		parent = p;
		// TODO Auto-generated constructor stub
	}

	@Override
	public Float getVOT() {
		// TODO Auto-generated method stub
		return parent.getVOT();
	}

	@Override
	public Mode getMode() {
		// TODO Auto-generated method stub
		return parent.getMode();
	}

}
