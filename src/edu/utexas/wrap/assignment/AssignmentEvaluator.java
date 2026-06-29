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
package edu.utexas.wrap.assignment;

import edu.utexas.wrap.net.Graph;

/**
 * Evaluates the quality or convergence of a traffic assignment solution.
 * Implementations compute metrics such as the relative gap between the
 * current solution cost and the theoretical minimum cost. The evaluator
 * processes individual containers and accumulates a network-wide measure.
 *
 * @param <T> the type of assignment container being evaluated
 * @author Will Alexander
 * @see GapEvaluator
 */
public interface AssignmentEvaluator<T extends AssignmentContainer> {

	/**
	 * Returns the current value of the evaluation metric (e.g., relative gap).
	 * A value of zero indicates perfect equilibrium.
	 *
	 * @return the current evaluation metric value
	 */
	public double getValue();
	
	/**
	 * Processes a single container, accumulating its contribution to the
	 * overall evaluation metric.
	 *
	 * @param container the assignment container to evaluate
	 * @param network the transportation network graph
	 */
	public void process(T container, Graph network);
	
	/**
	 * Resets the evaluator state for a new evaluation pass.
	 */
	public void initialize();
}
