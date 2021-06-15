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
package edu.utexas.wrap.assignment.bush;

import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.Node;

public class BushGapEvaluator implements BushEvaluator {
	
	private final Graph graph;
	
	public BushGapEvaluator(Graph g) {
		graph = g;
	}

	@Override
	public double getValue(Bush bush) {
		Double num = bush.incurredCost();
		Double denom = cheapestCostPossible(bush);
		if (num.isNaN() || denom.isNaN() || (((Double) (num/denom)).isNaN() && !num.equals(denom))) 
			throw new RuntimeException();
		else if (((Double) (num/denom)).isNaN())
			return 0;
		return num/denom - 1.0;
	}
	
	private Double cheapestCostPossible(Bush bush) {
		Node[] to = bush.getTopologicalOrder(true);
		double[] latent = new double[graph.numNodes()];
		PathCostCalculator pcc = new PathCostCalculator(bush);
		
		Double val = 0.0;
		
		for (int i = to.length-1; i > 0; i--) {
			if (to[i] == null) continue;
			double toDemand = bush.getDemand(to[i]) + latent[to[i].getOrder()];
			Link q = pcc.getqShort(to[i]);
			double linkCost = q.getPrice(bush);
			
			latent[q.getTail().getOrder()] += toDemand;
			
			val += linkCost*toDemand;
		}
		return val;
	}

}
