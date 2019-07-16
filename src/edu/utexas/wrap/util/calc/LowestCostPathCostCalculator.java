package edu.utexas.wrap.util.calc;

import java.util.Collection;
import java.util.Set;

import edu.utexas.wrap.assignment.bush.Bush;
import edu.utexas.wrap.assignment.bush.BushOrigin;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.Node;
import edu.utexas.wrap.util.FibonacciHeap;
import edu.utexas.wrap.util.FibonacciLeaf;

public class LowestCostPathCostCalculator extends Thread {
	public Graph graph;
	public Set<BushOrigin> origins;
	public Double val;

	public LowestCostPathCostCalculator(Graph g, Set<BushOrigin> origins) {
		graph = g;
		this.origins = origins;
	}
	public void run() {
		val = null;
		val = origins.parallelStream().flatMap(o -> o.getContainers().parallelStream()).mapToDouble(b -> lowestCostPathCost(b)).sum();
	}

	private double lowestCostPathCost(Bush b) {
		// TODO Auto-generated method stub
		Node orig = b.getOrigin().getNode();
		Collection<Node> nodes = graph.getNodes();
		FibonacciHeap<Node> Q = new FibonacciHeap<Node>(nodes.size());
		Double[] costs = new Double[graph.numNodes()];
		
		nodes.stream().filter(n -> !n.equals(orig)).forEach(n -> Q.add(n,Double.MAX_VALUE));
		Q.add(orig,0.0);
		double ret = 0.0;
		
		while (!Q.isEmpty()) {
			FibonacciLeaf<Node> u = Q.poll();
			if (u.key < Double.MAX_VALUE) {
				ret += u.key*b.getDemand(u.n);
				costs[u.n.getOrder()] = u.key;
			}
			
			for (Link uv : u.n.forwardStar()) {
				if (!uv.allowsClass(b.getVehicleClass())) continue;
				if (!b.isValidLink(uv)) continue;
				FibonacciLeaf<Node> v = Q.getLeaf(uv.getHead());
				Double alt = uv.getPrice(b.getVOT(),b.getVehicleClass())+u.key;
				
				if (alt < v.key) {
					Q.decreaseKey(v, alt);
				}
			}
		}
		
		return ret;
	}
}