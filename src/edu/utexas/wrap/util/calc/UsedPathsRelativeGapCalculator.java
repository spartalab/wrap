package edu.utexas.wrap.util.calc;

import java.util.Set;
import java.util.concurrent.atomic.DoubleAdder;

import edu.utexas.wrap.assignment.AssignmentContainer;
import edu.utexas.wrap.assignment.bush.Bush;
import edu.utexas.wrap.assignment.bush.PathCostCalculator;
import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;

public class UsedPathsRelativeGapCalculator extends Thread {
	public Double val;
	Graph graph;
	Set<AssignmentContainer> assignmentContainers;
	TotalSystemGeneralizedCostCalculator cc;
	
	public UsedPathsRelativeGapCalculator(Graph g, Set<AssignmentContainer> o, TotalSystemGeneralizedCostCalculator tc) {
		graph = g;
		assignmentContainers = o;
	}
	
	@Override
	public void run() {
		val = null;

		if (cc == null) {
			cc = new TotalSystemGeneralizedCostCalculator(graph, assignmentContainers);
			cc.start();
		}
		DoubleAdder denominator = new DoubleAdder();
		
		assignmentContainers.parallelStream()
		.filter(b -> b instanceof Bush)
		.map(b -> (Bush) b)
		.forEach(b ->{
			
			PathCostCalculator pcc = new PathCostCalculator(b);
			
			DemandMap dem = b.getDemandMap(); 
			dem.getZones().parallelStream().forEach(tsz -> {
				Float demand = dem.get(tsz);
				if (demand > 0.0F) {
					Node destination = graph.getNode(tsz.getID());
					
					denominator.add(pcc.getShortestPathCost(destination) * demand);
				}
			});
		});

		try{
			cc.join();
			val = (cc.val/denominator.sum()) - 1.0;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
