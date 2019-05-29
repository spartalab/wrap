package edu.utexas.wrap.assignment.bush;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.utexas.wrap.assignment.Origin;
import edu.utexas.wrap.demand.AutoDemandMap;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.Node;
import edu.utexas.wrap.util.FibonacciHeap;
import edu.utexas.wrap.util.FibonacciLeaf;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class BushOrigin extends Origin {
	private List<Bush> containers;
	private Map<Node, Link> initMap;

	/**Default constructor
	 * @param self the Node from which the Bushes will emanate
	 */
	public BushOrigin(Node self) {
		super(self);
		containers = new LinkedList<Bush>();
	}

	/** Build the origin's initial bush using Dijkstra's algorithm
	 * 
	 * Create from the full network an initial bush by finding the
	 * shortest path to each node in the network from the origin, 
	 * then selecting the paths which lead to destinations to which
	 * the origin has demand.
	 */	
	public void buildBush(Graph g, Float vot, AutoDemandMap destDemand, Mode c) {
		Bush b = new Bush(this, g, vot, destDemand, c);
		b.getOriginStructure();
		b.dumpFlow();
		containers.add(b);
	}

	/**Generate an initial bush (dag) by solving Dijkstra's Shortest Paths
	 * 
	 * To be called on initialization. Overwrites nodeL and qShort.
	 */
	public void buildInitMap(Graph g) {
		// Initialize every nodeL to infinity except this, the origin
		// Initialize the empty map of finalized nodes, the map of 
		// eligible nodes to contain this origin only, and the 
		// back-link mapping to be empty
		Collection<Node> nodes = g.getNodes();
		initMap = new Object2ObjectOpenHashMap<Node, Link>(nodes.size(),1.0f);
		FibonacciHeap<Node> Q = new FibonacciHeap<Node>(nodes.size(),1.0f);
		for (Node n : nodes) {
			if (!n.equals(getNode())) {
				Q.add(n, Double.MAX_VALUE);
			}
		}
		Q.add(getNode(), 0.0);

		while (!Q.isEmpty()) {
			FibonacciLeaf<Node> u = Q.poll();
			
			
			for (Link uv : g.outLinks(u.n)) {
//				if (!uv.allowsClass(c) || isInvalidConnector(uv)) continue;
				//If this link doesn't allow this bush's class of driver on the link, don't consider it
				
				FibonacciLeaf<Node> v = Q.getLeaf(uv.getHead());
				Double alt = uv.freeFlowTime()+u.key;
				if (alt<v.key) {
					Q.decreaseKey(v, alt);
					initMap.put(v.n, uv);
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.utexas.wrap.assignment.Origin#getContainers()
	 */
	@Override
	public List<Bush> getContainers() {
		return containers;
	}
	
	/** Get the total demand from the origin to the given Node 
	 * @param n the Node to whence demand should be calculated
	 * @return the total demand from all Bushes to the given Node
	 */
	public Double getDemand(Node n) {
		Double demand = 0.0;
		for(Bush bush : this.containers) {
			demand += bush.getDemand(n);
		}
		
		return demand;
	}
	
	/**Return the initial shortest path map
	 * @param g the graph to search for shortest paths
	 * @return the back-vector mapping of the shortest path tree
	 */
	public Map<Node, Link> getInitMap(Graph g) {
		if (initMap == null) buildInitMap(g);	//caching speeds this up
		return initMap;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return getNode().getID();
	}

	/**
	 * delete the cached shortest path tree once all Bushes are built
	 */
	public void deleteInitMap() {
		initMap = null;
	}

	public void loadBush(Graph g, Float vot, AutoDemandMap destDemand, Mode c) throws IOException {
		Bush b = new Bush(this, g, vot, destDemand, c);
		b.loadStructureFile();
		b.shortTopoSearch();
		b.longTopoSearch(false);
		b.dumpFlow();
		containers.add(b);
	}

}
