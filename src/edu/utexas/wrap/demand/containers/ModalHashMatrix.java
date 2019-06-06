package edu.utexas.wrap.demand.containers;

import edu.utexas.wrap.demand.ModalPAMatrix;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class ModalHashMatrix extends Object2ObjectOpenHashMap<Node, DemandHashMap> implements ODMatrix, ModalPAMatrix {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6104047201084019367L;
	private final Mode m;
	private Graph g;

	public ModalHashMatrix(Graph g, Mode mode) {
		this.g = g;
		this.m = mode;
	}

 	/* (non-Javadoc)
 	 * @see edu.utexas.wrap.demand.ODMatrix#getMode()
 	 */
 	public Mode getMode() {
 		return m;
 	}

	/* (non-Javadoc)
	 * @see edu.utexas.wrap.demand.ODMatrix#getDemand(edu.utexas.wrap.net.Node, edu.utexas.wrap.net.Node)
	 */
	public Float getDemand(Node origin, Node destination) {
		return get(origin) == null? 0.0F : get(origin).getOrDefault(destination,0.0F);
	}

	/* (non-Javadoc)
	 * @see edu.utexas.wrap.demand.ODMatrix#put(edu.utexas.wrap.net.Node, edu.utexas.wrap.net.Node, java.lang.Float)
	 */
	@Override
	public void put(Node origin, Node destination, Float demand) {
		putIfAbsent(origin, new DemandHashMap(getGraph()));
		((DemandHashMap) get(origin)).put(destination, demand);
		
	}

	/**
	 * @param i the Node from which trips originate
	 * @param d the map of demand from the given Node to other Nodes
	 */
	public void putDemand(Node i, DemandHashMap d) {
		put(i, d);
	}

	/* (non-Javadoc)
	 * @see edu.utexas.wrap.demand.PAMatrix#getVOT()
	 */
	@Override
	public float getVOT() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see edu.utexas.wrap.demand.ODMatrix#getGraph()
	 */
	@Override
	public Graph getGraph() {
		return g;
	}


}
