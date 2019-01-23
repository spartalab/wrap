package edu.utexas.wrap.assignment;

import edu.utexas.wrap.demand.AutomotiveDemandMap;
import edu.utexas.wrap.demand.AutomotiveOriginDestinationMatrix;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;

public abstract class AssignmentLoader {
	protected Graph graph;
	
	protected AssignmentLoader(Graph g) {
		this.graph = g;
	}
	
	protected abstract void addAll(AutomotiveOriginDestinationMatrix od);

	public abstract void add(Node root, AutomotiveDemandMap split);
	
	public abstract void load(Node root);
	
}
