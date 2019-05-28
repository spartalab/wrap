package edu.utexas.wrap.assignment.bush;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

import edu.utexas.wrap.assignment.AssignmentLoader;
import edu.utexas.wrap.demand.containers.AutoODHashMatrix;
import edu.utexas.wrap.demand.AutoDemandMap;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;

/**An instance of a {@link edu.utexas.wrap.assignment.AssignmentLoader}
 * used for loading demand into Bushes for use by Bush-based assignment
 * methods.
 * @author William
 *
 */
public class BushLoader extends AssignmentLoader {
	Map<Node, BushOriginBuilder> pool;
	ExecutorService p;
	Set<BushOrigin> origins;
	
	/**Default constructor
	 * @param g the graph onto which Origins should be built
	 */
	public BushLoader(Graph g) {
		super(g);
		pool = new HashMap<Node, BushOriginBuilder>();
		p = Executors.newWorkStealingPool();
		origins = new HashSet<BushOrigin>();
	}
	
	/* (non-Javadoc)
	 * @see edu.utexas.wrap.assignment.AssignmentLoader#add(edu.utexas.wrap.net.Node, edu.utexas.wrap.demand.containers.AutoDemandHashMap)
	 */
	public void add(Node o, AutoDemandMap map) {
		pool.putIfAbsent(o, new BushOriginBuilder(graph,o,origins));
		pool.get(o).addMap(map);
	} 
	
	/* (non-Javadoc)
	 * @see edu.utexas.wrap.assignment.AssignmentLoader#addAll(edu.utexas.wrap.demand.containers.AutoODHashMatrix)
	 */
	public void addAll(AutoODHashMatrix matrix) {
		for (Node o : matrix.keySet()) {
			add(o, matrix.get(o));
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.utexas.wrap.assignment.AssignmentLoader#load(edu.utexas.wrap.net.Node)
	 */
	public void load(Node o) {
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
			System.out.print("\rOrigins in queue: "+p.getQueuedSubmissionCount()+"\tActive: "+p.getActiveThreadCount());
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.print("\rInitial trips loaded            ");
		return origins;
	}

}
