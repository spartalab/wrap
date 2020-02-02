package edu.utexas.wrap.assignment.bush;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

import edu.utexas.wrap.assignment.OldAssignmentLoader;
import edu.utexas.wrap.demand.containers.AutoDemandPassthroughMap;
import edu.utexas.wrap.demand.containers.AutoODHashMatrix;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.demand.AutoDemandMap;
import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;

/**An instance of a {@link edu.utexas.wrap.assignment.OldAssignmentLoader}
 * used for loading demand into Bushes for use by Bush-based assignment
 * methods. This class first attempts to read a bush file given at the 
 * relative path "./{MD5 hash of input graph file}/{origin ID}/{Mode}-{VOT}.bush"
 * then builds from Dijkstra's shortest path algorithm if it can't be found.
 * @author William
 *
 */
@Deprecated
public class OldBushOriginFactory extends OldAssignmentLoader {
	Map<TravelSurveyZone, OldBushOriginBuilder> pool;
	ExecutorService p; 
	Set<BushOrigin> origins;
	
	/**Default constructor
	 * @param g the graph onto which Origins should be built
	 */
	public OldBushOriginFactory(Graph g) {
		super(g);
		pool = new HashMap<TravelSurveyZone, OldBushOriginBuilder>(g.numZones());
		p = Executors.newWorkStealingPool();
		origins = new HashSet<BushOrigin>();
	}
	
	/* (non-Javadoc)
	 * @see edu.utexas.wrap.assignment.AssignmentLoader#add(edu.utexas.wrap.net.Node, edu.utexas.wrap.demand.containers.AutoDemandHashMap)
	 */
	public void submit(TravelSurveyZone zone, AutoDemandMap map) {
		pool.putIfAbsent(zone, new BushOriginLoader(graph,zone,origins));
		pool.get(zone).addMap(map);
	} 
	
	/* (non-Javadoc)
	 * @see edu.utexas.wrap.assignment.AssignmentLoader#addAll(edu.utexas.wrap.demand.containers.AutoODHashMatrix)
	 */
	public void submitAll(AutoODHashMatrix matrix) {
		for (TravelSurveyZone o : matrix.getProducers()) {
			submit(o, matrix.get(o));
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.utexas.wrap.assignment.AssignmentLoader#load(edu.utexas.wrap.net.Node)
	 */
	public void start(TravelSurveyZone o) {
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
			System.out.print("\rLoading origins...\t"
					+ "In queue: "+String.format("%1$4s",p.getQueuedSubmissionCount())+"\t"
					+ "Active: "+p.getActiveThreadCount()+"\t"
					+ "Memory usage: "
					+ (Runtime.getRuntime().totalMemory()/1048576)+" MiB");
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.print("\rInitial trips loaded\tMemory usage: "+(Runtime.getRuntime().totalMemory()/1048576)+" MiB            ");
		return origins;
	}

	@Override
	public void submit(TravelSurveyZone root, DemandMap map, Mode mode, Float vot) {
		// TODO Auto-generated method stub
		submit(root, new AutoDemandPassthroughMap(map,mode,vot));
	}

}
