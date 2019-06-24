package edu.utexas.wrap.assignment.bush;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.Node;

public class SensitivityAnalyzer extends AlgorithmBOptimizer {

	public SensitivityAnalyzer(Graph g, Set<BushOrigin> o) {
		super(g, o);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void optimize() {
		super.optimize();
		Map<Link,Double> dtdX = new HashMap<Link,Double>(graph.numLinks(),1.0f);
		for (Link l : graph.getLinks()) {
			dtdX.put(l, l.tPrime());
		}
		
		//duplicate network with new link performance functions and zero flow
		Graph gprime = graph.getDerivativeGraph(dtdX);
		
		for (BushOrigin origin : origins) {
			for (Bush bush : origin.getContainers()) {
				for (Node destination : bush.getDemandMap().getNodes()){
					Map<Link,Double> dXdD = getDerivatives(gprime, origin, bush, destination);
				}
			}
		}	
	}

	private Map<Link, Double> getDerivatives(Graph gprime, BushOrigin origin, Bush bush, Node destination) {
		//TODO generate pseudo-OD with one destination's demand being 1 and all others 0
		
		//TODO solve for equilibrium
		
		//TODO record derivatives
		
		//TODO reset graph	
	}

}
