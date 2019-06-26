package edu.utexas.wrap.demand.containers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.ModalPAMatrix;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class ModalHashMatrix  implements ODMatrix, ModalPAMatrix {
	
	private final Mode m;
	private Graph g;
	protected Map<Node,DemandMap> map;
	
	public ModalHashMatrix(Graph g, Mode mode) {
		this.g = g;
		this.m = mode;
		map = Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap<Node, DemandMap>());
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
		return map.get(origin) == null? 0.0F : map.get(origin).getOrDefault(destination,0.0F);
	}

	/* (non-Javadoc)
	 * @see edu.utexas.wrap.demand.ODMatrix#put(edu.utexas.wrap.net.Node, edu.utexas.wrap.net.Node, java.lang.Float)
	 */
	@Override
	public void put(Node origin, Node destination, Float demand) {
		map.putIfAbsent(origin, new DemandHashMap(getGraph()));
		map.get(origin).put(destination, demand);
		
	}

	/**
	 * @param i the Node from which trips originate
	 * @param d the map of demand from the given Node to other Nodes
	 */
	public void putDemand(Node i, DemandHashMap d) {
		map.put(i, d);
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

	@Override
	public void toFile(File out) throws IOException {
		FileWriter o = null;
		try{
			o = new FileWriter(out);

			for (Node orig : map.keySet()) {
				DemandMap demand = map.get(orig);
				for (Node dest : demand.getNodes()) {
					o.write(""+orig.getID()+","+dest.getID()+","+demand.get(dest)+"\n");
				}
			}
		} finally {
			if (o != null) o.close();
		}
	}

	@Override
	public DemandMap getDemandMap(Node producer) {
		return map.get(producer);
	}

	@Override
	public Collection<Node> getProducers() {
		return map.keySet();
	}


}
