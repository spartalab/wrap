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

import java.util.Collection;
import java.util.function.ToDoubleFunction;

import edu.utexas.wrap.assignment.AssignmentBuilder;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.Node;
import edu.utexas.wrap.util.FibonacciHeap;
import edu.utexas.wrap.util.FibonacciLeaf;

public class BushBuilder implements AssignmentBuilder<Bush> {
	
	private ToDoubleFunction<Link> costFunction;
	
	public BushBuilder() {
		this(Link::freeFlowTime);
	}
	
	public BushBuilder(ToDoubleFunction<Link> costFunction) {
		this.costFunction = costFunction;
	}

	public void buildStructure(Bush bush, Graph network) {
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
