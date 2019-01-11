package edu.utexas.wrap.assignment.bush;

import java.util.HashSet;
import java.util.Set;

import edu.utexas.wrap.DestinationMatrix;
import edu.utexas.wrap.NetworkLoader;
import edu.utexas.wrap.OriginDestinationMatrix;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;

public class BushLoader extends NetworkLoader {
	Set<BushBuilder> pool;
	
	public BushLoader(Graph g) {
		super(g);
		pool = new HashSet<BushBuilder>();
	}
	
	public void add(Node o, DestinationMatrix map) {
		BushBuilder b = new BushBuilder(graph,o,map);
		pool.add(b);
		b.start();
	}
	
	public void addAll(OriginDestinationMatrix matrix) {
		for (Node o : matrix.keySet()) {
			add(o, matrix.get(o));
		}
	}
	
	public Set<BushOrigin> conclude() {
		Set<BushOrigin> origins = new HashSet<BushOrigin>();
		System.out.print("\r                                ");
		try {
			int size = pool.size();
			for (BushBuilder t : pool) {
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
