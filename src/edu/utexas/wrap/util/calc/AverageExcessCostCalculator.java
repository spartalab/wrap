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

import edu.utexas.wrap.assignment.AssignmentContainer;
import edu.utexas.wrap.net.Graph;

public class AverageExcessCostCalculator extends Thread {
	public Double val;
	Graph graph;
	Set<AssignmentContainer> origins;
	TotalSystemGeneralizedCostCalculator cc;
	LowestCostPathCostCalculator lc;
	
	public AverageExcessCostCalculator(Graph g, Set<AssignmentContainer> o, TotalSystemGeneralizedCostCalculator tc, LowestCostPathCostCalculator lc) {
		graph = g;
		origins = o;
		this.cc = tc;
		this.lc = lc;
	}
	
	@Override
	public void run() {
		//TODO: Modify for generalized cost
		if (cc == null) {
			cc = new TotalSystemGeneralizedCostCalculator(graph, origins);
			cc.start();
		}
		if (lc == null) {
			lc = new LowestCostPathCostCalculator(graph,origins);
			lc.start();
		}
		
		Double demand = origins.parallelStream().mapToDouble(b -> b.getDemandMap().totalDemand()).sum();
		
		val = null;
		try {
			cc.join();
			lc.join();
			val = (cc.val-lc.val)/demand;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
}