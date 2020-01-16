package edu.utexas.wrap.demand.containers;

import edu.utexas.wrap.demand.AutoDemandMap;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Graph;

public class AutoFixedSizeDemandMap extends FixedSizeDemandMap implements AutoDemandMap {
	private AutoODHashMatrix parent;
	
	public AutoFixedSizeDemandMap(Graph g, AutoODHashMatrix p) {
		super(g);
		parent = p;
	}

	@Override
	public Float getVOT() {
		return parent.getVOT();
	}

	@Override
	public Mode getMode() {
		return parent.getMode();
	}

}
