package edu.utexas.wrap.assignment.bush;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Link;

public class SensitivityAnalyzer extends AlgorithmBOptimizer {

	public SensitivityAnalyzer(Graph g, Set<BushOrigin> o) {
		super(g, o);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void optimize() {
		super.optimize();
		Map<Link,Double> derivs = new HashMap<Link,Double>(graph.numLinks(),1.0f);
		for (Link l : graph.getLinks()) {
			derivs.put(l, l.tPrime());
		}
		
		
		
	}

}
