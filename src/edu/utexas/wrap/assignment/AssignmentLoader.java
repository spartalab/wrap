package edu.utexas.wrap.assignment;

import edu.utexas.wrap.DemandMap;
import edu.utexas.wrap.OriginDestinationMatrix;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;

public abstract class AssignmentLoader {
	protected Graph graph;
	
	protected AssignmentLoader(Graph g) {
		this.graph = g;
	}
	
	protected abstract void addAll(OriginDestinationMatrix od);

	public abstract void add(Node root, DemandMap split);
	
	public abstract void load(Node root);
	
}
