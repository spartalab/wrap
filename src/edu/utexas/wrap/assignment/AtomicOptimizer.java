package edu.utexas.wrap.assignment;

import java.util.Map;

import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Link;

public interface AtomicOptimizer<T extends AssignmentContainer> 
			extends AssignmentOptimizer<T>{
	
	public void process(T container, 
			Graph network, 
			Map<Link,Double> flows,
			Map<Link,Double> greenShares, 
			Map<Link, Double> bottleneckDelays, 
			Map<Link, Double> map);

}
