package edu.utexas.wrap.assignment.bush;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.utexas.wrap.assignment.AssignmentLoader;
import edu.utexas.wrap.demand.containers.AutoODHashMatrix;
import edu.utexas.wrap.demand.containers.AutoDemandHashMap;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;

/**An instance of a {@link edu.utexas.wrap.assignment.AssignmentLoader}
 * used for loading demand into bushes for use by bush-based assignment
 * methods.
 * @author William
 *
 */
public class BushLoader extends AssignmentLoader {
	Map<Node, BushOriginBuilder> pool;
	
	public BushLoader(Graph g) {
		super(g);
		pool = new HashMap<Node, BushOriginBuilder>();
	}
	
	public void add(Node o, AutoDemandHashMap map) {
		pool.putIfAbsent(o, new BushOriginBuilder(graph,o));
		pool.get(o).addMap(map);
	} 
	
	public void addAll(AutoODHashMatrix matrix) {
		for (Node o : matrix.keySet()) {
			add(o, matrix.get(o));
		}
	}
	
	public void load(Node o) {
		pool.get(o).start();
	}
	
	public Set<BushOrigin> finishAll() {
		Set<BushOrigin> origins = new HashSet<BushOrigin>();
		System.out.print("\r                                ");
		try {
			int size = pool.size();
			for (BushOriginBuilder t : pool.values()) {
				System.out.print("\rFinalizing "+size+" origins     ");
				t.join();
				origins.add(t.orig);
				size--;
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.print("\rInitial trips loaded            ");
		return origins;
	}

}
