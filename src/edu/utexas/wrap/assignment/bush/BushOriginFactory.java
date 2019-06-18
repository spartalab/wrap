package edu.utexas.wrap.assignment.bush;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

import edu.utexas.wrap.assignment.AssignmentLoader;
import edu.utexas.wrap.demand.containers.AutoODMatrix;
import edu.utexas.wrap.demand.AutoDemandMap;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**An instance of a {@link edu.utexas.wrap.assignment.AssignmentLoader}
 * used for loading demand into Bushes for use by Bush-based assignment
 * methods. This class first attempts to read a bush file given at the 
 * relative path "./{MD5 hash of input graph file}/{origin ID}/{Mode}-{VOT}.bush"
 * then builds from Dijkstra's shortest path algorithm if it can't be found.
 * @author William
 *
 */
public class BushOriginFactory extends AssignmentLoader {
	Map<Node, BushOriginBuilder> pool;
	ExecutorService p;
	Set<BushOrigin> origins;
	
	/**Default constructor
	 * @param g the graph onto which Origins should be built
	 */
	public BushOriginFactory(Graph g) {
		super(g);
		pool = new Object2ObjectOpenHashMap<Node, BushOriginBuilder>(g.getNumZones());
		p = Executors.newWorkStealingPool();
		origins = new HashSet<BushOrigin>();
	}
	
	/* (non-Javadoc)
	 * @see edu.utexas.wrap.assignment.AssignmentLoader#add(edu.utexas.wrap.net.Node, edu.utexas.wrap.demand.containers.AutoDemandHashMap)
	 */
	public void submit(Node o, AutoDemandMap map) {
		pool.putIfAbsent(o, new BushOriginLoader(graph,o,origins));
		pool.get(o).addMap(map);
	} 
	
	/* (non-Javadoc)
	 * @see edu.utexas.wrap.assignment.AssignmentLoader#addAll(edu.utexas.wrap.demand.containers.AutoODHashMatrix)
	 */
	public void submitAll(AutoODMatrix matrix) {
		for (Node o : matrix.keySet()) {
			submit(o, matrix.get(o));
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.utexas.wrap.assignment.AssignmentLoader#load(edu.utexas.wrap.net.Node)
	 */
	public void start(Node o) {
		p.execute(pool.get(o));
	}
	
	/**Wait until all worker threads have finished. Print updates
	 * @return the set of completely loaded BushOrigins
	 */
	public Set<BushOrigin> finishAll() {
		ForkJoinPool p = (ForkJoinPool) this.p;
		System.out.print("\r                                ");
		p.shutdown();
		
		while (!p.isTerminated()) {
			System.out.print("\rLoading origins...\tIn queue: "+String.format("%1$4s",p.getQueuedSubmissionCount())+"\tActive: "+p.getActiveThreadCount());
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.print("\rInitial trips loaded            ");
		return origins;
	}

}
