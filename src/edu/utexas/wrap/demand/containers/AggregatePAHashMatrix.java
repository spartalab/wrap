package edu.utexas.wrap.demand.containers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class AggregatePAHashMatrix extends Object2ObjectOpenHashMap<Node, DemandHashMap> implements AggregatePAMatrix {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4656252954122078293L;
	private Graph g;

	public AggregatePAHashMatrix(Graph g) {
		this.g = g;
	}

	/**
	 * Insert the demand map for a given node
	 * @param i the Node from which there is demand
	 * @param d the map of demand from the given Node to other Nodes
	 */
	public void putDemandMap(Node i, DemandHashMap d) {
		put(i, d);
	}

	/* (non-Javadoc)
	 * @see edu.utexas.wrap.demand.PAMatrix#getDemand(edu.utexas.wrap.net.Node, edu.utexas.wrap.net.Node)
	 */
	@Override
	public Float getDemand(Node origin, Node destination) {
		// TODO Auto-generated method stub
		return get(origin) == null ? 0.0F : get(origin).getOrDefault(destination, 0.0F);
	}

	/* (non-Javadoc)
	 * @see edu.utexas.wrap.demand.PAMatrix#getVOT()
	 */
 	public float getVOT() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see edu.utexas.wrap.demand.PAMatrix#getGraph()
	 */
	@Override
	public Graph getGraph() {
		return g;
	}

	/* (non-Javadoc)
	 * @see edu.utexas.wrap.demand.PAMatrix#put(edu.utexas.wrap.net.Node, edu.utexas.wrap.net.Node, java.lang.Float)
	 */
	@Override
	public void put(Node origin, Node destination, Float demand) {
		putIfAbsent(origin,new DemandHashMap(g));
		((DemandMap) get(origin)).put(destination,demand);
		
	}

	@Override
	public void toFile(File out) throws IOException {
		FileWriter o = null;
		try{
			o = new FileWriter(out);

			for (Node orig : this.keys) {
				DemandHashMap demand = get(orig);
				for (Node dest : demand.keySet()) {
					o.write(""+orig.getID()+","+dest.getID()+","+demand.getFloat(dest)+"\n");
				}
			}
		} finally {
			if (o != null) o.close();
		}
	}

	@Override
	public DemandMap getDemandMap(Node producer) {
		// TODO Auto-generated method stub
		return get(producer);
	}

	@Override
	public Collection<Node> getProducers() {
		// TODO Auto-generated method stub
		return keySet();
	}

}
