package edu.utexas.wrap.assignment.bush;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.utexas.wrap.assignment.Origin;
import edu.utexas.wrap.demand.AutoDemandMap;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.CentroidConnector;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.Node;
import edu.utexas.wrap.net.TravelSurveyZone;
import edu.utexas.wrap.util.FibonacciHeap;
import edu.utexas.wrap.util.FibonacciLeaf;

@Deprecated
public class OldBushOrigin extends Origin {
	private List<Bush> containers;
	private BackVector[] initMap;

	/**Default constructor
	 * @param self the Node from which the Bushes will emanate
	 */
	public OldBushOrigin(TravelSurveyZone self) {
		super(self);
		containers = new ArrayList<Bush>();
	}

	/** Build the origin's initial bush using Dijkstra's algorithm
	 * 
	 * Create from the full network an initial bush by finding the
	 * shortest path to each node in the network from the origin, 
	 * then selecting the paths which lead to destinations to which
	 * the origin has demand.
	 */	
	public Bush buildBush(Graph g, Float vot, AutoDemandMap destDemand, Mode c) {
		Bush b = new Bush(this, g, vot, destDemand, c);
		b.getOriginStructure();
		b.dumpFlow();
		containers.add(b);
		return b;
	}

	/**Generate an initial bush (dag) by solving Dijkstra's Shortest Paths
	 * 
	 * To be called on initialization. Overwrites nodeL and qShort.
	 */
	public BackVector[] getShortestPathTree(Graph g) {
		// Initialize every nodeL to infinity except this, the origin
		// Initialize the empty map of finalized nodes, the map of 
		// eligible nodes to contain this origin only, and the 
		// back-link mapping to be empty
		Collection<Node> nodes = g.getNodes();
		BackVector[] initMap = new BackVector[nodes.size()];
		FibonacciHeap<Node> Q = new FibonacciHeap<Node>(nodes.size(),1.0f);
		for (Node n : nodes) {
			if (!n.equals(getNode())) {
				Q.add(n, Double.MAX_VALUE);
			}
		}
		Q.add(getNode(), 0.0);

		while (!Q.isEmpty()) {
			FibonacciLeaf<Node> u = Q.poll();
			
			
			for (Link uv : u.n.forwardStar()) {
				if (!isValidLink(uv)) continue;
//				if (!uv.allowsClass(c) || isInvalidConnector(uv)) continue;
				//If this link doesn't allow this bush's class of driver on the link, don't consider it
				//This was removed to allow flow onto all links for the initial bush, and any illegal
				//flow will be removed on the first flow shift due to high price
				
				FibonacciLeaf<Node> v = Q.getLeaf(uv.getHead());
				Double alt = uv.freeFlowTime()+u.key;
				if (alt<v.key) {
					Q.decreaseKey(v, alt);
					initMap[v.n.getOrder()] = uv;
				}
			}
		}
		return initMap;
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
	public BackVector[] getInitMap(Graph g) {
		if (initMap == null) {
			initMap = getShortestPathTree(g);	//caching speeds this up
		}
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

	/**Load a bush onto initial paths from the default file
	 * @param g	the graph to which 
	 * @param vot the vot of the bush to be loaded
	 * @param destDemand the DemandMap associated with the bush
	 * @param c the mode of the bush to be loaded
	 * @throws IOException
	 */
	public void loadBush(Graph g, Float vot, AutoDemandMap destDemand, Mode c) throws IOException {
		Bush b = new Bush(this, g, vot, destDemand, c);
		b.fromDefaultFile();
		b.dumpFlow();
		containers.add(b);
	}
	
	public boolean isValidLink(Link uv) {
		//If the link is a centroid connector
		return uv instanceof CentroidConnector? 
				//that doesn't lead from the origin and
				uv.getTail().equals(getNode())? true :
					//leads from a different centroid instead
					(uv.getHead().isCentroid() && !uv.getTail().isCentroid())? true:
						//then we can't use the link in the bush
						false
				//Otherwise, we can
				: true;
	}

	public boolean add(Bush newBush) {
		// TODO Auto-generated method stub
		return containers.add(newBush);
	}

}
