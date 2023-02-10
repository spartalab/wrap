/*
    wrap - free and open-source urban transportation modeling software
    Copyright (C) 2017 the wrap project, The University of Texas at Austin

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.utexas.wrap.util.calc;

import java.util.Set;
import java.util.concurrent.atomic.DoubleAdder;

import edu.utexas.wrap.assignment.AssignmentContainer;
import edu.utexas.wrap.assignment.MarginalVOTPolicy;
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
	MarginalVOTPolicy votPolicy;
	
	public UsedPathsRelativeGapCalculator(
			Graph g, 
			Set<AssignmentContainer> o, 
			TotalSystemGeneralizedCostCalculator tc,
			MarginalVOTPolicy votPolicy) {
		graph = g;
		assignmentContainers = o;
		this.votPolicy = votPolicy;
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
			
			PathCostCalculator pcc = new PathCostCalculator(b, votPolicy);
			
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
