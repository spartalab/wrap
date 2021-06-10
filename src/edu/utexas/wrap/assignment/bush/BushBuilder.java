package edu.utexas.wrap.assignment.bush;

import java.util.Collection;
import java.util.function.ToDoubleFunction;

import edu.utexas.wrap.assignment.AssignmentBuilder;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.Node;
import edu.utexas.wrap.util.FibonacciHeap;
import edu.utexas.wrap.util.FibonacciLeaf;

public class BushBuilder implements AssignmentBuilder<Bush> {
	
	private Graph network;
	private ToDoubleFunction<Link> costFunction;
	
	public BushBuilder(Graph network) {
		this(network, Link::freeFlowTime);
	}
	
	public BushBuilder(Graph network, ToDoubleFunction<Link> costFunction) {
		this.network = network;
		this.costFunction = costFunction;
	}

	public void buildStructure(Bush bush) {
		// Initialize every nodeL to infinity except this, the origin
		// Initialize the empty map of finalized nodes, the map of 
		// eligible nodes to contain this origin only, and the 
		// back-link mapping to be empty
		Collection<Node> nodes = network.getNodes();
		BackVector[] initMap = new BackVector[nodes.size()];
		FibonacciHeap Q = new FibonacciHeap(nodes.size(),1.0f);
		for (Node n : nodes) {
			if (!n.getID().equals(bush.root().getID())) {
				Q.add(n, Double.MAX_VALUE);
			}
			else Q.add(n,0.0);
		}
//		Q.add(bush.root().node(), 0.0);

		while (!Q.isEmpty()) {
			FibonacciLeaf u = Q.poll();
			
			
			for (Link uv : u.node.forwardStar()) {
				if (!bush.canUseLink(uv)) continue;
//				if (!uv.allowsClass(c) || isInvalidConnector(uv)) continue;
				//If this link doesn't allow this bush's class of driver on the link, don't consider it
				//This was removed to allow flow onto all links for the initial bush, and any illegal
				//flow will be removed on the first flow shift due to high price
				
				FibonacciLeaf v = Q.getLeaf(uv.getHead());
				Double alt = costFunction.applyAsDouble(uv)+u.key;
				if (alt<v.key) {
					Q.decreaseKey(v, alt);
					initMap[v.node.getOrder()] = uv;
				}
			}
		}
		
		bush.setQ(initMap);
	}

}
