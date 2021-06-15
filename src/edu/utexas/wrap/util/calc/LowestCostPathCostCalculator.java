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

public class LowestCostPathCostCalculator extends Thread {
	public Graph graph;
	public Set<AssignmentContainer> containers;
	public Double val;

	public LowestCostPathCostCalculator(Graph g, Set<AssignmentContainer> containers) {
		graph = g;
		this.containers = containers;
	}
	public void run() {
		val = null;
		val = containers.parallelStream().mapToDouble(b -> graph.cheapestCostPossible(b)).sum();
	}
}