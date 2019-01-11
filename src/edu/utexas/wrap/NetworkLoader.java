package edu.utexas.wrap;

import java.util.Set;

import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;

public abstract class NetworkLoader {
	protected Graph graph;
	
	protected NetworkLoader(Graph g) {
		this.graph = g;
	}
	
	public abstract void add(Node o, DestinationMatrix d);
	
	public abstract void addAll(OriginDestinationMatrix od);
	
	public abstract Set<? extends Origin> conclude();
}
